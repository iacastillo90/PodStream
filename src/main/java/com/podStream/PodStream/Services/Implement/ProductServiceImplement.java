package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.ProductDTO;
import com.podStream.PodStream.Models.*;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.CategoryProductRepository;
import com.podStream.PodStream.Repositories.Jpa.ClientRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRatingRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRepository;
import com.podStream.PodStream.Services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar productos.
 */
@Service
public class ProductServiceImplement implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImplement.class);
    private static final String PRODUCT_CACHE_KEY = "product:active:";
    private static final long PRODUCT_TTL_MINUTES = 60;

    private final ProductRepository productRepository;
    private final CategoryProductRepository categoryRepository;
    private final ClientRepository clientRepository;
    private final ProductRatingRepository productRatingRepository;
    private final ProductSearchServiceImplement productSearchServiceImplement;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public ProductServiceImplement(
            ProductRepository productRepository,
            CategoryProductRepository categoryRepository,
            ClientRepository clientRepository,
            ProductRatingRepository productRatingRepository,
            ProductSearchServiceImplement productSearchServiceImplement,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.clientRepository = clientRepository;
        this.productRatingRepository = productRatingRepository;
        this.productSearchServiceImplement = productSearchServiceImplement;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, Authentication authentication) {
        logger.info("Creating product: {}", productDTO.getName());
        validateAuthentication(authentication, "ROLE_ADMIN");

        Product product = mapToEntity(productDTO);
        product.setActive(true);
        Product savedProduct = productRepository.save(product);
        productSearchServiceImplement.syncProduct(savedProduct);
        redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + savedProduct.getId(), savedProduct, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementProductCreated();
        return new ProductDTO(savedProduct);
    }

    @Override
    public ProductDTO getProduct(Long id) {
        logger.info("Fetching product with id: {}", id);
        String cacheKey = PRODUCT_CACHE_KEY + id;
        Product cachedProduct = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProduct != null && cachedProduct.isActive()) {
            logger.info("Product id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementProductCacheHit();
            return new ProductDTO(cachedProduct);
        }

        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        redisTemplate.opsForValue().set(cacheKey, product, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementProductFetched();
        return new ProductDTO(product);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productRepository.findByActiveTrue();
        podStreamPrometheusConfig.incrementProductFetched();
        return products.stream()
                .map(product -> {
                    redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + product.getId(), product, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);
                    return new ProductDTO(product);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByName(String name) {
        logger.info("Fetching products by name: {}", name);
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
        podStreamPrometheusConfig.incrementProductFetched();
        return products.stream()
                .map(product -> {
                    redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + product.getId(), product, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);
                    return new ProductDTO(product);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        logger.info("Fetching products by category: {}", categoryId);
        List<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId);
        podStreamPrometheusConfig.incrementProductFetched();
        return products.stream()
                .map(product -> {
                    redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + product.getId(), product, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);
                    return new ProductDTO(product);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByPriceRange(double minPrice, double maxPrice) {
        logger.info("Fetching products by price range: {}-{}", minPrice, maxPrice);
        List<Product> products = productRepository.findByPriceBetweenAndActiveTrue(minPrice, maxPrice);
        podStreamPrometheusConfig.incrementProductFetched();
        return products.stream()
                .map(product -> {
                    redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + product.getId(), product, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);
                    return new ProductDTO(product);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getTopPopularProducts(int limit) {
        logger.info("Fetching top {} popular products", limit);
        List<Product> products = productRepository.findByOrderBySalesCountDesc(PageRequest.of(0, limit));
        podStreamPrometheusConfig.incrementProductFetched();
        return products.stream()
                .map(product -> {
                    redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + product.getId(), product, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);
                    return new ProductDTO(product);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, Authentication authentication) {
        logger.info("Updating product with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN");

        Product existingProduct = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        updateEntity(existingProduct, productDTO);
        Product updatedProduct = productRepository.save(existingProduct);
        productSearchServiceImplement.syncProduct(updatedProduct);
        redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + updatedProduct.getId(), updatedProduct, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementProductUpdated();
        return new ProductDTO(updatedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateStock(Long id, Integer newStock, String updatedBy, Authentication authentication) {
        logger.info("Updating stock for product id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN");
        if (newStock < 0) {
            podStreamPrometheusConfig.incrementProductErrors();
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        int oldStock = product.getStock();
        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);
        productSearchServiceImplement.syncProduct(updatedProduct);
        redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + updatedProduct.getId(), updatedProduct, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);

        logger.info("Stock updated for product {} from {} to {} by {}", id, oldStock, newStock, updatedBy);
        podStreamPrometheusConfig.incrementProductUpdated();
        if (newStock <= 10) {
            podStreamPrometheusConfig.incrementProductLowStock();
        }
        return new ProductDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Authentication authentication) {
        logger.info("Deleting product with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN");

        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
        productSearchServiceImplement.syncProduct(product);
        redisTemplate.delete(PRODUCT_CACHE_KEY + id);

        podStreamPrometheusConfig.incrementProductDeleted();
    }

    @Override
    @Transactional
    public void addRating(Long id, int rating, Long clientId, Authentication authentication) {
        logger.info("Adding rating {} for product id: {} by client id: {}", rating, id, clientId);
        validateAuthentication(authentication, "ROLE_USER");

        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));

        ProductRating productRating = new ProductRating();
        productRating.setProduct(product);
        productRating.setClient(client);
        productRating.setRating(rating);
        productRatingRepository.save(productRating);

        // Actualizar averageRating y totalRatingPoints
        List<ProductRating> ratings = productRatingRepository.findByProductId(id);
        double total = ratings.stream().mapToInt(ProductRating::getRating).sum();
        product.setTotalRatingPoints(total);
        product.setAverageRating(ratings.isEmpty() ? 0 : total / ratings.size());
        productRepository.save(product);
        productSearchServiceImplement.syncProduct(product);
        redisTemplate.opsForValue().set(PRODUCT_CACHE_KEY + product.getId(), product, PRODUCT_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementProductUpdated();
    }

    private Product mapToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setSalesCount(dto.getSalesCount());
        if (dto.getCategoryId() != null) {
            CategoryProduct category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        }
        product.setColor(dto.getColor());
        product.setDiscount(dto.getDiscount());
        product.setImage(dto.getImage());
        product.setAverageRating(dto.getAverageRating());
        product.setImageCollection(dto.getImageCollection());
        return product;
    }

    private void updateEntity(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setSalesCount(dto.getSalesCount());
        if (dto.getCategoryId() != null) {
            CategoryProduct category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
        product.setColor(dto.getColor());
        product.setDiscount(dto.getDiscount());
        product.setImage(dto.getImage());
        product.setAverageRating(dto.getAverageRating());
        product.setImageCollection(dto.getImageCollection());
    }

    private void validateAuthentication(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementProductErrors();
            throw new SecurityException("Authentication required");
        }
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> List.of(roles).contains(auth.getAuthority()));
        if (!hasRole) {
            logger.warn("User {} does not have required roles: {}", authentication.getName(), String.join(", ", roles));
            podStreamPrometheusConfig.incrementProductErrors();
            throw new SecurityException("Insufficient permissions");
        }
    }
}