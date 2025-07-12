package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.ClientInteraction;
import com.podStream.PodStream.Models.InteractionType;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticClientInteractionRepository extends ElasticsearchRepository<ClientInteraction, Long> {
    List<ClientInteraction> findByInteractionType(InteractionType interactionType);
    List<ClientInteraction> findByClientId(Long clientId);
    List<ClientInteraction> findByProductId(Long productId);
}
