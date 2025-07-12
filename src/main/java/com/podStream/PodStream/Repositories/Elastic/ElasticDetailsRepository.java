package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.Details;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticDetailsRepository extends ElasticsearchRepository<Details, Long> {
    List<Details> findByDescriptionContainingIgnoreCase(String description);
    List<Details> findByProductId(Long productId);
}
