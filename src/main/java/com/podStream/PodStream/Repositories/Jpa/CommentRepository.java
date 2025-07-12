package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByClientIdAndActiveTrue(Long clientId);
    List<Comment> findByProductIdAndActiveTrue(Long productId);
}
