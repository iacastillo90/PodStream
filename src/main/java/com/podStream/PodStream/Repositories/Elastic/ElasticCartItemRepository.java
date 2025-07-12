package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.CartItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticCartItemRepository extends ElasticsearchRepository<CartItem, Long> {
    List<CartItem> findByProductId(Long productId);
}