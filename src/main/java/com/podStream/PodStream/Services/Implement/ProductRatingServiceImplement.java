package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.ProductRatingDTO;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.ProductRating;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.ClientRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRatingRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRepository;
import com.podStream.PodStream.Services.ProductRatingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar calificaciones de productos en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@Service
public class ProductRatingServiceImplement implements ProductRatingService {

    private static final Logger logger = LoggerFactory.getLogger(ProductRatingServiceImplement.class);
    private static final String RATING_CACHE_KEY = "rating:active:";
    private static final long RATING_TTL_MINUTES = 60;

    private final ProductRatingRepository productRatingRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final ProductSearchServiceImplement productSearchServiceImplement;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public ProductRatingServiceImplement(
            ProductRatingRepository productRatingRepository,
            ProductRepository productRepository,
            ClientRepository clientRepository,
            ProductSearchServiceImplement productSearchServiceImplement,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.productRatingRepository = productRatingRepository;
        this.productRepository = productRepository;
        this.clientRepository = clientRepository;
        this.productSearchServiceImplement = productSearchServiceImplement;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    @Override
    @Transactional
    public ProductRatingDTO createRating(ProductRatingDTO ratingDTO, Authentication authentication) {
        logger.info("Creating rating for product id: {} by client id: {}", ratingDTO.getProductId(), ratingDTO.getClientId());
        validateAuthentication(authentication, "ROLE_USER");

        Product product = productRepository.findById(ratingDTO.getProductId())
                .filter(Product::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + ratingDTO.getProductId()));
        Client client = clientRepository.findById(ratingDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + ratingDTO.getClientId()));

        // Verificar si ya existe una calificación activa
        productRatingRepository.findByClientIdAndProductIdAndActiveTrue(ratingDTO.getClientId(), ratingDTO.getProductId())
                .ifPresent(r -> {
                    throw new IllegalStateException("Client has already rated this product");
                });

        ProductRating rating = new ProductRating();
        rating.setProduct(product);
        rating.setClient(client);
        rating.setRating(ratingDTO.getRating());
        rating.setActive(true);
        ProductRating savedRating = productRatingRepository.save(rating);

        // Actualizar promedio en Product
        updateProductAverageRating(product);
        redisTemplate.opsForValue().set(RATING_CACHE_KEY + savedRating.getId(), savedRating, RATING_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementRatingCreated();

        return new ProductRatingDTO(savedRating);
    }

    @Override
    public ProductRatingDTO getRating(Long id) {
        logger.info("Fetching rating with id: {}", id);
        String cacheKey = RATING_CACHE_KEY + id;
        ProductRating cachedRating = (ProductRating) redisTemplate.opsForValue().get(cacheKey);
        if (cachedRating != null && cachedRating.isActive()) {
            logger.info("Rating id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementRatingCacheHit();
            return new ProductRatingDTO(cachedRating);
        }

        ProductRating rating = productRatingRepository.findById(id)
                .filter(ProductRating::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + id));
        redisTemplate.opsForValue().set(cacheKey, rating, RATING_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementRatingFetched();
        return new ProductRatingDTO(rating);
    }

    @Override
    public List<ProductRatingDTO> getRatingsByProduct(Long productId) {
        logger.info("Fetching ratings for product id: {}", productId);
        List<ProductRating> ratings = productRatingRepository.findByProductIdAndActiveTrue(productId);
        podStreamPrometheusConfig.incrementRatingFetched();
        return ratings.stream()
                .map(rating -> {
                    redisTemplate.opsForValue().set(RATING_CACHE_KEY + rating.getId(), rating, RATING_TTL_MINUTES, TimeUnit.MINUTES);
                    return new ProductRatingDTO(rating);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductRatingDTO> getRatingsByClient(Long clientId) {
        logger.info("Fetching ratings for client id: {}", clientId);
        List<ProductRating> ratings = productRatingRepository.findByClientIdAndActiveTrue(clientId);
        podStreamPrometheusConfig.incrementRatingFetched();
        return ratings.stream()
                .map(rating -> {
                    redisTemplate.opsForValue().set(RATING_CACHE_KEY + rating.getId(), rating, RATING_TTL_MINUTES, TimeUnit.MINUTES);
                    return new ProductRatingDTO(rating);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductRatingDTO updateRating(Long id, ProductRatingDTO ratingDTO, Authentication authentication) {
        logger.info("Updating rating with id: {}", id);
        validateAuthentication(authentication, "ROLE_USER");

        ProductRating existingRating = productRatingRepository.findById(id)
                .filter(ProductRating::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + id));

        // Validar que el cliente autenticado es el dueño de la calificación
        if (!existingRating.getClient().getId().equals(ratingDTO.getClientId())) {
            podStreamPrometheusConfig.incrementRatingErrors();
            throw new SecurityException("Only the rating owner can update it");
        }

        Product product = productRepository.findById(ratingDTO.getProductId())
                .filter(Product::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + ratingDTO.getProductId()));
        existingRating.setRating(ratingDTO.getRating());
        ProductRating updatedRating = productRatingRepository.save(existingRating);

        // Actualizar promedio en Product
        updateProductAverageRating(product);
        redisTemplate.opsForValue().set(RATING_CACHE_KEY + updatedRating.getId(), updatedRating, RATING_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementRatingUpdated();

        return new ProductRatingDTO(updatedRating);
    }

    @Override
    @Transactional
    public void deleteRating(Long id, Authentication authentication) {
        logger.info("Deleting rating with id: {}", id);
        validateAuthentication(authentication, "ROLE_USER");

        ProductRating rating = productRatingRepository.findById(id)
                .filter(ProductRating::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + id));

        // Validar que el cliente autenticado es el dueño de la calificación
        String username = authentication.getName();
        if (!rating.getClient().getUsername().equals(username)) {
            podStreamPrometheusConfig.incrementRatingErrors();
            throw new SecurityException("Only the rating owner can delete it");
        }

        rating.setActive(false);
        productRatingRepository.save(rating);
        updateProductAverageRating(rating.getProduct());
        redisTemplate.delete(RATING_CACHE_KEY + id);
        podStreamPrometheusConfig.incrementRatingDeleted();
    }

    private void updateProductAverageRating(Product product) {
        Double average = productRatingRepository.findAverageRatingByProductId(product.getId());
        product.setAverageRating(average != null ? average : 0.0);
        product.setTotalRatingPoints(productRatingRepository.findByProductIdAndActiveTrue(product.getId())
                .stream().mapToDouble(ProductRating::getRating).sum());
        productRepository.save(product);
        productSearchServiceImplement.syncProduct(product);
    }

    private void validateAuthentication(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementRatingErrors();
            throw new SecurityException("Authentication required");
        }
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> List.of(roles).contains(auth.getAuthority()));
        if (!hasRole) {
            logger.warn("User {} does not have required roles: {}", authentication.getName(), String.join(", ", roles));
            podStreamPrometheusConfig.incrementRatingErrors();
            throw new SecurityException("Insufficient permissions");
        }
    }
}


