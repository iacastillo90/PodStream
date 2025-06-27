
package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.DTOS.ClientInteractionRequest;
import com.podStream.PodStream.Models.ClientInteraction;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Repositories.ClientInteractionRepository;
import com.podStream.PodStream.Repositories.ClientRepository;
import com.podStream.PodStream.Repositories.ProductRepository;
import com.podStream.PodStream.Services.ClientInteractionService;
import com.podStream.PodStream.Services.Events.ClientInteractionEvent;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClientInteractionServiceImplement implements ClientInteractionService {

    private static final Logger logger = LoggerFactory.getLogger(ClientInteractionServiceImplement.class);

    private final ClientInteractionRepository clientInteractionRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ClientInteractionServiceImplement(
            ClientInteractionRepository clientInteractionRepository,
            ClientRepository clientRepository,
            ProductRepository productRepository,
            ApplicationEventPublisher eventPublisher) {
        this.clientInteractionRepository = clientInteractionRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ClientInteraction recordInteraction(ClientInteractionRequest request) {
        logger.info("Recording interaction for user {} and product {}", request.getUserId(), request.getProductId());

        Client client = clientRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + request.getUserId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductId()));

        ClientInteraction interaction = new ClientInteraction();
        interaction.setClient(client);
        interaction.setProduct(product);
        interaction.setInteractionType(request.getInteractionType());
        interaction.setQuantity(request.getQuantity());
        interaction.setSessionId(request.getSessionId());
        interaction.setTimestamp(LocalDateTime.now());

        ClientInteraction savedInteraction = clientInteractionRepository.save(interaction);

        // Disparar el evento
        eventPublisher.publishEvent(new ClientInteractionEvent(this, savedInteraction));
        logger.info("Published ClientInteractionEvent for interaction ID {}", savedInteraction.getId());

        return savedInteraction;
    }
}
