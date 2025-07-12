package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.ProductDTO;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.ProductDocument;
import com.podStream.PodStream.Repositories.Jpa.ProductRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticProductRepository;
import com.podStream.PodStream.Services.ProductSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Servicio para búsqueda y filtrado de productos en Elasticsearch con fallback a MySQL.
 */
@Service
public class ProductSearchServiceImplement implements ProductSearchService {

    private static final Logger logger = LoggerFactory.getLogger(ProductSearchServiceImplement.class);
    private static final String SEARCH_CACHE_KEY = "search:products:";
    private static final long SEARCH_TTL_MINUTES = 30;

    private final ElasticProductRepository elasticProductRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public ProductSearchServiceImplement(
            ElasticProductRepository elasticProductRepository,
            ProductRepository productRepository,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.elasticProductRepository = elasticProductRepository;
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    public List<ProductDTO> searchProducts(String query) {
        String cacheKey = SEARCH_CACHE_KEY + "query:" + query.toLowerCase();
        @SuppressWarnings("unchecked")
        List<ProductDTO> cachedResults = (List<ProductDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResults != null) {
            logger.info("Search results for query {} retrieved from cache", query);
            podStreamPrometheusConfig.incrementSearchCacheHit();
            return cachedResults;
        }

        try {
            logger.info("Searching products with query: {}", query);
            List<ProductDocument> documents = elasticProductRepository.findByNameContainingOrDescriptionContainingAndActiveTrue(query, query);
            List<ProductDTO> results = documents.stream()
                    .map(doc -> productRepository.findById(doc.getId())
                            .filter(Product::isActive)
                            .map(ProductDTO::new)
                            .orElse(null))
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(cacheKey, results, SEARCH_TTL_MINUTES, TimeUnit.MINUTES);
            podStreamPrometheusConfig.incrementSearchSuccess();
            return results;
        } catch (Exception e) {
            logger.error("Error searching in Elasticsearch, falling back to MySQL: {}", e.getMessage());
            podStreamPrometheusConfig.incrementSearchErrors();
            List<Product> products = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query);
            List<ProductDTO> results = products.stream().map(ProductDTO::new).collect(Collectors.toList());
            redisTemplate.opsForValue().set(cacheKey, results, SEARCH_TTL_MINUTES, TimeUnit.MINUTES);
            return results;
        }
    }

    public List<ProductDTO> filterProducts(Long categoryId, Double minPrice, Double maxPrice) {
        String cacheKey = SEARCH_CACHE_KEY + "filter:cat" + categoryId + ":price" + minPrice + "-" + maxPrice;
        @SuppressWarnings("unchecked")
        List<ProductDTO> cachedResults = (List<ProductDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResults != null) {
            logger.info("Filter results for category {} and price {}-{} retrieved from cache", categoryId, minPrice, maxPrice);
            podStreamPrometheusConfig.incrementSearchCacheHit();
            return cachedResults;
        }

        try {
            logger.info("Filtering products by category ID: {}, price: {}-{}", categoryId, minPrice, maxPrice);
            List<ProductDocument> documents = elasticProductRepository.findByCategoryIdAndPriceBetweenAndActiveTrue(categoryId, minPrice, maxPrice);
            List<ProductDTO> results = documents.stream()
                    .map(doc -> productRepository.findById(doc.getId())
                            .filter(Product::isActive)
                            .map(ProductDTO::new)
                            .orElse(null))
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(cacheKey, results, SEARCH_TTL_MINUTES, TimeUnit.MINUTES);
            podStreamPrometheusConfig.incrementSearchSuccess();
            return results;
        } catch (Exception e) {
            logger.error("Error filtering in Elasticsearch, falling back to MySQL: {}", e.getMessage());
            podStreamPrometheusConfig.incrementSearchErrors();
            List<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                    .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
            List<ProductDTO> results = products.stream().map(ProductDTO::new).collect(Collectors.toList());
            redisTemplate.opsForValue().set(cacheKey, results, SEARCH_TTL_MINUTES, TimeUnit.MINUTES);
            return results;
        }
    }

    public void syncProduct(Product product) {
        try {
            ProductDocument document = new ProductDocument(product);
            elasticProductRepository.save(document);
            logger.info("Product {} synchronized with Elasticsearch", product.getId());
            podStreamPrometheusConfig.incrementProductSyncSuccess();
        } catch (Exception e) {
            logger.error("Error synchronizing product {} with Elasticsearch: {}", product.getId(), e.getMessage());
            podStreamPrometheusConfig.incrementProductSyncErrors();
        }
    }
}