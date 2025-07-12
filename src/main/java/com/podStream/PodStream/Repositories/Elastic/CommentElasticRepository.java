package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.Comment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CommentElasticRepository extends ElasticsearchRepository<Comment, Long> {
    List<Comment> findByBodyContainingIgnoreCase(String body);
    List<Comment> findByProductId(Long productId);
}
