package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.Answers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface AnswersRepository extends JpaRepository<Answers, Long> {
}
