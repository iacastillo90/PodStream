package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.DTOS.RecommendationResponseDTO;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.ProductRating;
import com.podStream.PodStream.Repositories.ProductRatingRepository;
import com.podStream.PodStream.Repositories.ProductRepository;
import com.podStream.PodStream.Services.RecommendationService;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de recomendaciones para PodStream.
 * Utiliza Apache Spark ALS para generar recomendaciones personalizadas,
 * con inicialización perezosa de Spark para un arranque robusto.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.2
 * @since 2025-06-26
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImplement implements RecommendationService {

    private final ProductRepository productRepository;
    private final ProductRatingRepository productRatingRepository;

    private volatile SparkSession sparkSession;
    private volatile ALSModel alsModel;
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImplement.class);
    private final Object sparkSessionLock = new Object();

    @PreDestroy
    public void cleanup() {
        synchronized (sparkSessionLock) {
            if (sparkSession != null) {
                sparkSession.stop();
                logger.info("SparkSession stopped successfully.");
            }
        }
    }

    private SparkSession getOrCreateSparkSession() {
        if (this.sparkSession == null) {
            synchronized (sparkSessionLock) {
                if (this.sparkSession == null) {
                    logger.info("SparkSession is null. Initializing a new session...");
                    try {
                        this.sparkSession = SparkSession.builder()
                                .appName("PodStreamRecommendations")
                                .master("local[*]")
                                .config("spark.sql.shuffle.partitions", "10")
                                .config("spark.driver.memory", "2g")
                                .config("spark.executor.memory", "2g")
                                .getOrCreate();
                        logger.info("SparkSession initialized successfully in local mode.");
                    } catch (Exception e) {
                        logger.error("Failed to initialize SparkSession: {}", e.getMessage(), e);
                        throw new RuntimeException("Spark initialization failed", e);
                    }
                }
            }
        }
        return this.sparkSession;
    }

    @Scheduled(fixedRate = 86_400_000) // Cada 24 horas
    public void trainModel() {
        logger.info("Starting recommendation model training...");
        try {
            List<ProductRating> ratings = productRatingRepository.findAll();
            if (ratings.isEmpty()) {
                logger.warn("No ratings available for training. Skipping model training.");
                return;
            }

            SparkSession spark = getOrCreateSparkSession();

            Dataset<Row> ratingsDataset = spark.createDataset(ratings, Encoders.bean(ProductRating.class))
                    .select(
                            functions.col("client.id").as("userId"),
                            functions.col("product.id").as("productId"),
                            functions.col("rating").as("rating").cast(DataTypes.FloatType)
                    );

            ratingsDataset.cache();
            Dataset<Row>[] splits = ratingsDataset.randomSplit(new double[]{0.8, 0.2});
            Dataset<Row> training = splits[0];
            Dataset<Row> test = splits[1];

            ALS als = new ALS()
                    .setMaxIter(10)
                    .setRegParam(0.01)
                    .setUserCol("userId")
                    .setItemCol("productId")
                    .setRatingCol("rating")
                    .setColdStartStrategy("drop");

            this.alsModel = als.fit(training);

            Dataset<Row> predictions = this.alsModel.transform(test);
            RegressionEvaluator evaluator = new RegressionEvaluator()
                    .setMetricName("rmse")
                    .setLabelCol("rating")
                    .setPredictionCol("prediction");

            double rmse = evaluator.evaluate(predictions);
            logger.info("Model trained successfully. Root-mean-square error = {}", rmse);
            ratingsDataset.unpersist();

        } catch (Exception e) {
            logger.error("Error during model training: {}", e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "userRecommendations", key = "#userId")
    public List<RecommendationResponseDTO> getRecommendationsForUser(Long userId, int howMany) {
        if (userId == null || userId <= 0) {
            logger.warn("Invalid userId: {}. Falling back to popular products.", userId);
            return getPopularProducts(howMany);
        }
        if (this.alsModel == null) {
            logger.warn("ALS model is not trained. Falling back to popular products.");
            return getPopularProducts(howMany);
        }

        SparkSession spark = getOrCreateSparkSession();

        try {
            Dataset<Row> users = spark.createDataFrame(
                    Collections.singletonList(RowFactory.create(userId)),
                    new StructType(new StructField[]{
                            new StructField("userId", DataTypes.LongType, false, Metadata.empty())
                    })
            );

            Dataset<Row> recommendations = this.alsModel.recommendForUserSubset(users, howMany);
            List<Row> recommendationRows = recommendations.select("recommendations.productId", "recommendations.rating").collectAsList();

            if (recommendationRows.isEmpty() || recommendationRows.get(0).isNullAt(0)) {
                logger.info("No recommendations generated for user {}. Falling back to popular products.", userId);
                return getPopularProducts(howMany);
            }

            List<Long> productIds = recommendationRows.get(0).getList(0);
            List<Float> scores = recommendationRows.get(0).getList(1);

            return productIds.stream()
                    .map(id -> {
                        Product product = productRepository.findById(id).orElse(null);
                        if (product == null) {
                            logger.warn("Recommended product with ID {} not found in database.", id);
                            return null;
                        }
                        int index = productIds.indexOf(id);
                        return RecommendationResponseDTO.builder()
                                .id(product.getId())
                                .productName(product.getName())
                                .category(product.getCategory() != null ? product.getCategory().toString() : null)
                                .image(product.getImage())
                                .score(scores.get(index).doubleValue())
                                .build();
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error generating recommendations for user {}: {}", userId, e.getMessage(), e);
            return getPopularProducts(howMany);
        }
    }

    @Override
    public List<RecommendationResponseDTO> getPopularProducts(int howMany) {
        logger.info("Fetching {} popular products.", howMany);
        try {
            Pageable pageable = PageRequest.of(0, howMany);
            List<Product> popularProducts = productRepository.findTopPopularProducts(pageable);
            return popularProducts.stream()
                    .map(product -> RecommendationResponseDTO.builder()
                            .id(product.getId())
                            .productName(product.getName())
                            .category(product.getCategory() != null ? product.getCategory().toString() : null)
                            .image(product.getImage())
                            .score((double) product.getSalesCount()) // Usar salesCount como score
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching popular products: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "itemRecommendations", key = "#productId")
    public List<RecommendationResponseDTO> getContentBasedRecommendations(Long productId, int howMany) {
        if (productId == null || productId <= 0) {
            logger.warn("Invalid productId: {}. Returning empty list.", productId);
            return Collections.emptyList();
        }

        try {
            Product targetProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

            List<Product> similarProducts = productRepository.findAll().stream()
                    .filter(p -> !p.getId().equals(targetProduct.getId()))
                    .filter(p -> p.getCategory() != null && p.getCategory().equals(targetProduct.getCategory()))
                    .filter(p -> p.getColor() != null && p.getColor().equals(targetProduct.getColor()))
                    .sorted((p1, p2) -> Double.compare(p2.getAveragePoints(), p1.getAveragePoints()))
                    .limit(howMany)
                    .collect(Collectors.toList());

            return similarProducts.stream()
                    .map(product -> RecommendationResponseDTO.builder()
                            .id(product.getId())
                            .productName(product.getName())
                            .category(product.getCategory() != null ? product.getCategory().toString() : null)
                            .image(product.getImage())
                            .score(calculateSimilarityScore(targetProduct, product))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error generating content-based recommendations for product {}: {}", productId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private double calculateSimilarityScore(Product p1, Product p2) {
        double score = 0.0;
        if (p1.getCategory() != null && p1.getCategory().equals(p2.getCategory())) score += 0.5;
        if (p1.getColor() != null && p1.getColor().equals(p2.getColor())) score += 0.3;
        double priceDiff = Math.abs(p1.getPrice() - p2.getPrice()) / Math.max(p1.getPrice(), p2.getPrice());
        score += (1 - priceDiff) * 0.2;
        return score;
    }
}