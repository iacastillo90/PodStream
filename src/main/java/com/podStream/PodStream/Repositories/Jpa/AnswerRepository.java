package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.Answers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answers, Long> {

    List<Answers> findByCommentId(Long commentId);

    boolean existsByIdAndClientId(Long id, Long clientId);

}