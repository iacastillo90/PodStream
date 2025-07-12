package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Repositorio Elasticsearch para búsquedas avanzadas de ProductDocument.
 */
public interface ElasticProductRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByNameContainingOrDescriptionContainingAndActiveTrue(String name, String description);
    List<ProductDocument> findByCategoryIdAndPriceBetweenAndActiveTrue(Long categoryId, Double minPrice, Double maxPrice);
    List<ProductDocument> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<ProductDocument> findByActiveTrue();
}