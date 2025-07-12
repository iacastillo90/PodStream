package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.CategoryProductDTO;
import com.podStream.PodStream.Models.CategoryProduct;
import com.podStream.PodStream.Repositories.Jpa.CategoryProductRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticCategoryProductRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRepository;
import com.podStream.PodStream.Services.CategoryProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryProductServiceImplement implements CategoryProductService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryProductServiceImplement.class);
    private static final String CATEGORY_KEY_PREFIX = "category:active:";
    private static final long CATEGORY_TTL_MINUTES = 1440; // 1 día para categorías activas

    private final CategoryProductRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ElasticCategoryProductRepository searchRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    @Override
    public List<CategoryProductDTO> findAll(Authentication authentication) {
        logger.info("Fetching all active categories");
        validateAuthentication(authentication);
        List<CategoryProduct> categories = categoryRepository.findByActiveTrue();
        podStreamPrometheusConfig.incrementCategoryFetched();
        return categories.stream()
                .map(category -> {
                    redisTemplate.opsForValue().set(CATEGORY_KEY_PREFIX + category.getId(), category, CATEGORY_TTL_MINUTES, TimeUnit.MINUTES);
                    return new CategoryProductDTO(category);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CategoryProductDTO findById(Long id, Authentication authentication) {
        logger.info("Fetching category with id: {}", id);
        validateAuthentication(authentication);

        String cacheKey = CATEGORY_KEY_PREFIX + id;
        CategoryProduct cachedCategory = (CategoryProduct) redisTemplate.opsForValue().get(cacheKey);
        if (cachedCategory != null && cachedCategory.isActive()) {
            logger.info("Category id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementCategoryCacheHit();
            return new CategoryProductDTO(cachedCategory);
        }

        CategoryProduct category = categoryRepository.findById(id)
                .filter(CategoryProduct::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        redisTemplate.opsForValue().set(cacheKey, category, CATEGORY_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementCategoryFetched();
        return new CategoryProductDTO(category);
    }

    @Override
    @Transactional
    public CategoryProductDTO save(CategoryProductDTO categoryDTO, Authentication authentication) {
        logger.info("Saving category: {}", categoryDTO.getName());
        validateAuthentication(authentication, true);
        if (categoryRepository.findByNameAndActiveTrue(categoryDTO.getName()).isPresent()) {
            podStreamPrometheusConfig.incrementCategoryErrors();
            throw new IllegalArgumentException("Category name already exists: " + categoryDTO.getName());
        }

        CategoryProduct category = new CategoryProduct();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setActive(true);

        CategoryProduct savedCategory = categoryRepository.save(category);
        searchRepository.save(savedCategory);
        redisTemplate.opsForValue().set(CATEGORY_KEY_PREFIX + savedCategory.getId(), savedCategory, CATEGORY_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementCategoryCreated();
        return new CategoryProductDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryProductDTO update(Long id, CategoryProductDTO categoryDTO, Authentication authentication) {
        logger.info("Updating category with id: {}", id);
        validateAuthentication(authentication, true);

        CategoryProduct existing = categoryRepository.findById(id)
                .filter(CategoryProduct::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        if (!existing.getName().equals(categoryDTO.getName()) &&
                categoryRepository.findByNameAndActiveTrue(categoryDTO.getName()).isPresent()) {
            podStreamPrometheusConfig.incrementCategoryErrors();
            throw new IllegalArgumentException("Category name already exists: " + categoryDTO.getName());
        }

        existing.setName(categoryDTO.getName());
        existing.setDescription(categoryDTO.getDescription());

        CategoryProduct updatedCategory = categoryRepository.save(existing);
        searchRepository.save(updatedCategory);
        redisTemplate.opsForValue().set(CATEGORY_KEY_PREFIX + updatedCategory.getId(), updatedCategory, CATEGORY_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementCategoryUpdated();
        return new CategoryProductDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteById(Long id, Authentication authentication) {
        logger.info("Deleting category with id: {}", id);
        validateAuthentication(authentication, true);

        CategoryProduct category = categoryRepository.findById(id)
                .filter(CategoryProduct::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        if (productRepository.existsByCategoryId(id)) {
            podStreamPrometheusConfig.incrementCategoryErrors();
            throw new IllegalStateException("Cannot delete category with associated products");
        }

        category.setActive(false);
        categoryRepository.save(category);
        searchRepository.save(category);
        redisTemplate.delete(CATEGORY_KEY_PREFIX + id);

        podStreamPrometheusConfig.incrementCategoryDeleted();
    }

    private void validateAuthentication(Authentication authentication, boolean adminRequired) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementCategoryErrors();
            throw new SecurityException("Authentication required");
        }
        if (adminRequired && !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            logger.warn("User {} not authorized to perform admin operation", authentication.getName());
            podStreamPrometheusConfig.incrementCategoryErrors();
            throw new SecurityException("Admin role required");
        }
    }

    private void validateAuthentication(Authentication authentication) {
        validateAuthentication(authentication, false);
    }
}
