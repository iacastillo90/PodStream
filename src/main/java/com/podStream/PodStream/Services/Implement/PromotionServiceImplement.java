package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.PromotionDTO;
import com.podStream.PodStream.Models.Promotion;
import com.podStream.PodStream.Repositories.Jpa.PromotionRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticPromotionRepository;
import com.podStream.PodStream.Services.PromotionService;
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
 * Implementación del servicio para gestionar promociones en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-09
 */
@Service
public class PromotionServiceImplement implements PromotionService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionServiceImplement.class);
    private static final String PROMOTION_CACHE_KEY = "promotion:active:";
    private static final long PROMOTION_TTL_MINUTES = 60;

    private final PromotionRepository promotionRepository;
    private final ElasticPromotionRepository elasticPromotionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public PromotionServiceImplement(
            PromotionRepository promotionRepository,
            ElasticPromotionRepository elasticPromotionRepository,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.promotionRepository = promotionRepository;
        this.elasticPromotionRepository = elasticPromotionRepository;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    @Override
    @Transactional
    public PromotionDTO createPromotion(PromotionDTO promotionDTO, Authentication authentication) {
        logger.info("Creating promotion with code: {}", promotionDTO.getCode());
        validateAuthentication(authentication, "ROLE_ADMIN");

        if (promotionRepository.findByCodeAndActiveTrue(promotionDTO.getCode()).isPresent()) {
            podStreamPrometheusConfig.incrementPromotionErrors();
            throw new IllegalArgumentException("Promotion code already exists");
        }

        Promotion promotion = mapToEntity(promotionDTO);
        promotion.setActive(true);
        Promotion savedPromotion = promotionRepository.save(promotion);
        elasticPromotionRepository.save(savedPromotion);
        redisTemplate.opsForValue().set(PROMOTION_CACHE_KEY + savedPromotion.getId(), savedPromotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(PROMOTION_CACHE_KEY + "code:" + savedPromotion.getCode(), savedPromotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementPromotionCreated();
        return new PromotionDTO(savedPromotion);
    }

    @Override
    public PromotionDTO getPromotion(Long id) {
        logger.info("Fetching promotion with id: {}", id);
        String cacheKey = PROMOTION_CACHE_KEY + id;
        Promotion cachedPromotion = (Promotion) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPromotion != null && cachedPromotion.getActive()) {
            logger.info("Promotion id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementPromotionCacheHit();
            return new PromotionDTO(cachedPromotion);
        }

        Promotion promotion = promotionRepository.findById(id)
                .filter(Promotion::getActive)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found with id: " + id));
        redisTemplate.opsForValue().set(cacheKey, promotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementPromotionFetched();
        return new PromotionDTO(promotion);
    }

    @Override
    public PromotionDTO getPromotionByCode(String code) {
        logger.info("Fetching promotion with code: {}", code);
        String cacheKey = PROMOTION_CACHE_KEY + "code:" + code;
        Promotion cachedPromotion = (Promotion) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPromotion != null && cachedPromotion.getActive()) {
            logger.info("Promotion code: {} retrieved from cache", code);
            podStreamPrometheusConfig.incrementPromotionCacheHit();
            return new PromotionDTO(cachedPromotion);
        }

        Promotion promotion = promotionRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found with code: " + code));
        redisTemplate.opsForValue().set(cacheKey, promotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(PROMOTION_CACHE_KEY + promotion.getId(), promotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementPromotionFetched();
        return new PromotionDTO(promotion);
    }

    @Override
    public List<PromotionDTO> getAllPromotions() {
        logger.info("Fetching all active promotions");
        List<Promotion> promotions = promotionRepository.findByActiveTrue();
        podStreamPrometheusConfig.incrementPromotionFetched();
        return promotions.stream()
                .map(promotion -> {
                    redisTemplate.opsForValue().set(PROMOTION_CACHE_KEY + promotion.getId(), promotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);
                    redisTemplate.opsForValue().set(PROMOTION_CACHE_KEY + "code:" + promotion.getCode(), promotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);
                    return new PromotionDTO(promotion);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PromotionDTO updatePromotion(Long id, PromotionDTO promotionDTO, Authentication authentication) {
        logger.info("Updating promotion with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN");

        Promotion existingPromotion = promotionRepository.findById(id)
                .filter(Promotion::getActive)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found with id: " + id));

        if (!existingPromotion.getCode().equals(promotionDTO.getCode()) &&
                promotionRepository.findByCodeAndActiveTrue(promotionDTO.getCode()).isPresent()) {
            podStreamPrometheusConfig.incrementPromotionErrors();
            throw new IllegalArgumentException("Promotion code already exists");
        }

        updateEntity(existingPromotion, promotionDTO);
        Promotion updatedPromotion = promotionRepository.save(existingPromotion);
        elasticPromotionRepository.save(updatedPromotion);
        redisTemplate.opsForValue().set(PROMOTION_CACHE_KEY + updatedPromotion.getId(), updatedPromotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(PROMOTION_CACHE_KEY + "code:" + updatedPromotion.getCode(), updatedPromotion, PROMOTION_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementPromotionUpdated();
        return new PromotionDTO(updatedPromotion);
    }

    @Override
    @Transactional
    public void deletePromotion(Long id, Authentication authentication) {
        logger.info("Deleting promotion with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN");

        Promotion promotion = promotionRepository.findById(id)
                .filter(Promotion::getActive)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found with id: " + id));
        promotion.setActive(false);
        promotionRepository.save(promotion);
        elasticPromotionRepository.deleteById(id);
        redisTemplate.delete(PROMOTION_CACHE_KEY + id);
        redisTemplate.delete(PROMOTION_CACHE_KEY + "code:" + promotion.getCode());

        podStreamPrometheusConfig.incrementPromotionDeleted();
    }

    private Promotion mapToEntity(PromotionDTO dto) {
        Promotion promotion = new Promotion();
        promotion.setCode(dto.getCode());
        promotion.setDiscountPercentage(dto.getDiscountPercentage());
        promotion.setValidUntil(dto.getValidUntil());
        promotion.setActive(dto.getActive());
        return promotion;
    }

    private void updateEntity(Promotion promotion, PromotionDTO dto) {
        promotion.setCode(dto.getCode());
        promotion.setDiscountPercentage(dto.getDiscountPercentage());
        promotion.setValidUntil(dto.getValidUntil());
        promotion.setActive(dto.getActive());
    }

    private void validateAuthentication(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementPromotionErrors();
            throw new SecurityException("Authentication required");
        }
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> List.of(roles).contains(auth.getAuthority()));
        if (!hasRole) {
            logger.warn("User {} does not have required roles: {}", authentication.getName(), String.join(", ", roles));
            podStreamPrometheusConfig.incrementPromotionErrors();
            throw new SecurityException("Insufficient permissions");
        }
    }
}