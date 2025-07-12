package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.Cart;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticCartRepository extends ElasticsearchRepository<Cart, Long> {
    List<Cart> findByItemsProductNameContaining(String productName);
}
