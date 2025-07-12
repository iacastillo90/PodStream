package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.ClientInteraction;
import com.podStream.PodStream.Models.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientInteractionRepository extends JpaRepository<ClientInteraction, Long> {
    List<ClientInteraction> findByClientIdAndActiveTrue(Long clientId);
    List<ClientInteraction> findByProductIdAndActiveTrue(Long productId);
    List<ClientInteraction> findByInteractionTypeAndActiveTrue(InteractionType interactionType);
}
