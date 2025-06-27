package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.ClientInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ClientInteractionRepository extends JpaRepository <ClientInteraction, Long> {


}
