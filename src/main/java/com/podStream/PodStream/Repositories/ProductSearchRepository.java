package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.CategoryProduct;
import com.podStream.PodStream.Models.ProductDocument;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

@RepositoryRestResource
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
    
    List<ProductDocument> findByCategoryIdAndPriceBetween(Long categoryId, Double minPrice, Double maxPrice);
}
