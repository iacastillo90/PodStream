package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.Details;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface DetailsRepository extends JpaRepository<Details, Long> {
}
