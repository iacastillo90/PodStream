package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.ClientInteractionDTO;
import com.podStream.PodStream.DTOS.ClientInteractionRequest;
import com.podStream.PodStream.Models.ClientInteraction;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Repositories.Jpa.ClientInteractionRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticClientInteractionRepository;
import com.podStream.PodStream.Repositories.Jpa.ClientRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRepository;
import com.podStream.PodStream.Services.ClientInteractionService;
import com.podStream.PodStream.Services.Events.ClientInteractionEvent;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ClientInteractionServiceImplement implements ClientInteractionService {

    private static final Logger logger = LoggerFactory.getLogger(ClientInteractionServiceImplement.class);
    private static final String INTERACTION_KEY_PREFIX = "interaction:active:";
    private static final long INTERACTION_TTL_MINUTES = 1440; // 1 día para interacciones

    private final ClientInteractionRepository clientInteractionRepository;
    private final ElasticClientInteractionRepository searchRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public ClientInteractionServiceImplement(
            ClientInteractionRepository clientInteractionRepository,
            ElasticClientInteractionRepository searchRepository,
            ClientRepository clientRepository,
            ProductRepository productRepository,
            ApplicationEventPublisher eventPublisher,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.clientInteractionRepository = clientInteractionRepository;
        this.searchRepository = searchRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    @Override
    @Transactional
    public ClientInteractionDTO recordInteraction(ClientInteractionRequest request, Authentication authentication) {
        logger.info("Recording interaction for user {} and product {}", request.getUserId(), request.getProductId());
        Long clientId = validateAuthentication(authentication);
        validateClientOwnership(clientId, request.getUserId());

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
        interaction.setActive(true);

        ClientInteraction savedInteraction = clientInteractionRepository.save(interaction);
        searchRepository.save(savedInteraction);
        redisTemplate.opsForValue().set(INTERACTION_KEY_PREFIX + savedInteraction.getId(), savedInteraction, INTERACTION_TTL_MINUTES, TimeUnit.MINUTES);

        eventPublisher.publishEvent(new ClientInteractionEvent(this, savedInteraction));
        logger.info("Published ClientInteractionEvent for interaction ID {}", savedInteraction.getId());

        podStreamPrometheusConfig.incrementInteractionCreated();
        return new ClientInteractionDTO(savedInteraction);
    }

    @Override
    public ClientInteractionDTO getInteraction(Long id, Authentication authentication) {
        logger.info("Fetching interaction with id: {}", id);
        Long clientId = validateAuthentication(authentication);

        String cacheKey = INTERACTION_KEY_PREFIX + id;
        ClientInteraction cachedInteraction = (ClientInteraction) redisTemplate.opsForValue().get(cacheKey);
        if (cachedInteraction != null && cachedInteraction.isActive()) {
            logger.info("Interaction id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementInteractionCacheHit();
            return new ClientInteractionDTO(cachedInteraction);
        }

        ClientInteraction interaction = clientInteractionRepository.findById(id)
                .filter(ClientInteraction::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Interaction not found with id: " + id));
        validateClientOwnership(clientId, interaction.getClient().getId());

        redisTemplate.opsForValue().set(cacheKey, interaction, INTERACTION_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementInteractionFetched();
        return new ClientInteractionDTO(interaction);
    }

    @Override
    public List<ClientInteractionDTO> getInteractionsByClient(Long clientId, Authentication authentication) {
        logger.info("Fetching interactions for client: {}", clientId);
        Long authenticatedClientId = validateAuthentication(authentication);
        validateClientOwnership(authenticatedClientId, clientId);

        List<ClientInteraction> interactions = clientInteractionRepository.findByClientIdAndActiveTrue(clientId);
        podStreamPrometheusConfig.incrementInteractionFetched();
        return interactions.stream()
                .map(interaction -> {
                    redisTemplate.opsForValue().set(INTERACTION_KEY_PREFIX + interaction.getId(), interaction, INTERACTION_TTL_MINUTES, TimeUnit.MINUTES);
                    return new ClientInteractionDTO(interaction);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClientInteractionDTO updateInteraction(Long id, ClientInteractionRequest request, Authentication authentication) {
        logger.info("Updating interaction with id: {}", id);
        Long clientId = validateAuthentication(authentication);

        ClientInteraction interaction = clientInteractionRepository.findById(id)
                .filter(ClientInteraction::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Interaction not found with id: " + id));
        validateClientOwnership(clientId, interaction.getClient().getId());

        Client client = clientRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + request.getUserId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductId()));

        interaction.setClient(client);
        interaction.setProduct(product);
        interaction.setInteractionType(request.getInteractionType());
        interaction.setQuantity(request.getQuantity());
        interaction.setSessionId(request.getSessionId());

        ClientInteraction updatedInteraction = clientInteractionRepository.save(interaction);
        searchRepository.save(updatedInteraction);
        redisTemplate.opsForValue().set(INTERACTION_KEY_PREFIX + updatedInteraction.getId(), updatedInteraction, INTERACTION_TTL_MINUTES, TimeUnit.MINUTES);

        eventPublisher.publishEvent(new ClientInteractionEvent(this, updatedInteraction));
        logger.info("Published ClientInteractionEvent for updated interaction ID {}", updatedInteraction.getId());

        podStreamPrometheusConfig.incrementInteractionUpdated();
        return new ClientInteractionDTO(updatedInteraction);
    }

    @Override
    @Transactional
    public void deleteInteraction(Long id, Authentication authentication) {
        logger.info("Deleting interaction with id: {}", id);
        Long clientId = validateAuthentication(authentication);

        ClientInteraction interaction = clientInteractionRepository.findById(id)
                .filter(ClientInteraction::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Interaction not found with id: " + id));
        validateClientOwnership(clientId, interaction.getClient().getId());

        interaction.setActive(false);
        clientInteractionRepository.save(interaction);
        searchRepository.save(interaction);
        redisTemplate.delete(INTERACTION_KEY_PREFIX + id);

        podStreamPrometheusConfig.incrementInteractionDeleted();
    }

    private Long validateAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementInteractionErrors();
            throw new SecurityException("Authentication required");
        }
        return Long.valueOf(authentication.getName());
    }

    private void validateClientOwnership(Long authenticatedClientId, Long requestedClientId) {
        if (!authenticatedClientId.equals(requestedClientId)) {
            logger.warn("Client id: {} not authorized to access interactions for client: {}", authenticatedClientId, requestedClientId);
            podStreamPrometheusConfig.incrementInteractionErrors();
            throw new SecurityException("Not authorized to access this client's interactions");
        }
    }
}