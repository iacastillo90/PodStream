package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.CategoryProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticCategoryProductRepository extends ElasticsearchRepository<CategoryProduct, Long> {
    List<CategoryProduct> findByNameContainingIgnoreCase(String name);
}
