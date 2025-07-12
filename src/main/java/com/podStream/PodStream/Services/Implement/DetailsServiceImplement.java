package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.DetailsDTO;
import com.podStream.PodStream.DTOS.DetailsRequestDTO;
import com.podStream.PodStream.Models.Details;
import com.podStream.PodStream.Models.PurchaseOrder;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Repositories.Jpa.DetailsRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticDetailsRepository;
import com.podStream.PodStream.Repositories.Jpa.PurchaseOrderRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRepository;
import com.podStream.PodStream.Services.DetailsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DetailsServiceImplement implements DetailsService {

    private static final Logger logger = LoggerFactory.getLogger(DetailsServiceImplement.class);
    private static final String DETAILS_KEY_PREFIX = "details:active:";
    private static final long DETAILS_TTL_MINUTES = 1440; // 1 día para detalles

    private final DetailsRepository detailsRepository;
    private final ElasticDetailsRepository searchRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public DetailsServiceImplement(
            DetailsRepository detailsRepository,
            ElasticDetailsRepository searchRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            ProductRepository productRepository,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.detailsRepository = detailsRepository;
        this.searchRepository = searchRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    @Override
    @Transactional
    public DetailsDTO createDetails(DetailsRequestDTO request, Authentication authentication) {
        logger.info("Creating details for purchase order {} and product {}", request.getPurchaseOrderId(), request.getProductId());
        Long clientId = validateAuthentication(authentication);

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                .filter(PurchaseOrder::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found: " + request.getPurchaseOrderId()));
        validateOrderOwnership(clientId, purchaseOrder);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductId()));
        if (product.getStock() < request.getQuantity()) {
            podStreamPrometheusConfig.incrementDetailsErrors();
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);

        Details details = new Details();
        details.setProductName(request.getProductName());
        details.setQuantity(request.getQuantity());
        details.setPrice(request.getPrice());
        details.setDescription(request.getDescription());
        details.setPurchaseOrder(purchaseOrder);
        details.setProduct(product);
        details.setCreatedDate(LocalDateTime.now());
        details.setActive(true);

        Details savedDetails = detailsRepository.save(details);
        searchRepository.save(savedDetails);
        redisTemplate.opsForValue().set(DETAILS_KEY_PREFIX + savedDetails.getId(), savedDetails, DETAILS_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementDetailsCreated();
        return new DetailsDTO(savedDetails);
    }

    @Override
    public DetailsDTO getDetails(Long id, Authentication authentication) {
        logger.info("Fetching details with id: {}", id);
        Long clientId = validateAuthentication(authentication);

        String cacheKey = DETAILS_KEY_PREFIX + id;
        Details cachedDetails = (Details) redisTemplate.opsForValue().get(cacheKey);
        if (cachedDetails != null && cachedDetails.isActive()) {
            logger.info("Details id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementDetailsCacheHit();
            return new DetailsDTO(cachedDetails);
        }

        Details details = detailsRepository.findById(id)
                .filter(Details::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Details not found with id: " + id));
        validateOrderOwnership(clientId, details.getPurchaseOrder());

        redisTemplate.opsForValue().set(cacheKey, details, DETAILS_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementDetailsFetched();
        return new DetailsDTO(details);
    }

    @Override
    public List<DetailsDTO> getDetailsByPurchaseOrder(Long purchaseOrderId, Authentication authentication) {
        logger.info("Fetching details for purchase order: {}", purchaseOrderId);
        Long clientId = validateAuthentication(authentication);

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .filter(PurchaseOrder::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found: " + purchaseOrderId));
        validateOrderOwnership(clientId, purchaseOrder);

        List<Details> details = detailsRepository.findByPurchaseOrderIdAndActiveTrue(purchaseOrderId);
        podStreamPrometheusConfig.incrementDetailsFetched();
        return details.stream()
                .map(detail -> {
                    redisTemplate.opsForValue().set(DETAILS_KEY_PREFIX + detail.getId(), detail, DETAILS_TTL_MINUTES, TimeUnit.MINUTES);
                    return new DetailsDTO(detail);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DetailsDTO> getDetailsByProduct(Long productId, Authentication authentication) {
        logger.info("Fetching details for product: {}", productId);
        Long clientId = validateAuthentication(authentication);

        List<Details> details = detailsRepository.findByProductIdAndActiveTrue(productId);
        // Filtrar solo los detalles de órdenes que pertenezcan al cliente autenticado
        details = details.stream()
                .filter(detail -> detail.getPurchaseOrder().getClient().getId().equals(clientId))
                .collect(Collectors.toList());

        podStreamPrometheusConfig.incrementDetailsFetched();
        return details.stream()
                .map(detail -> {
                    redisTemplate.opsForValue().set(DETAILS_KEY_PREFIX + detail.getId(), detail, DETAILS_TTL_MINUTES, TimeUnit.MINUTES);
                    return new DetailsDTO(detail);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DetailsDTO updateDetails(Long id, DetailsRequestDTO request, Authentication authentication) {
        logger.info("Updating details with id: {}", id);
        Long clientId = validateAuthentication(authentication);

        Details details = detailsRepository.findById(id)
                .filter(Details::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Details not found with id: " + id));
        validateOrderOwnership(clientId, details.getPurchaseOrder());

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                .filter(PurchaseOrder::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found: " + request.getPurchaseOrderId()));
        validateOrderOwnership(clientId, purchaseOrder);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductId()));

        // Revertir stock anterior
        Product oldProduct = details.getProduct();
        oldProduct.setStock(oldProduct.getStock() + details.getQuantity());
        productRepository.save(oldProduct);

        // Validar y actualizar stock nuevo
        if (product.getStock() < request.getQuantity()) {
            podStreamPrometheusConfig.incrementDetailsErrors();
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);

        details.setProductName(request.getProductName());
        details.setQuantity(request.getQuantity());
        details.setPrice(request.getPrice());
        details.setDescription(request.getDescription());
        details.setPurchaseOrder(purchaseOrder);
        details.setProduct(product);

        Details updatedDetails = detailsRepository.save(details);
        searchRepository.save(updatedDetails);
        redisTemplate.opsForValue().set(DETAILS_KEY_PREFIX + updatedDetails.getId(), updatedDetails, DETAILS_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementDetailsUpdated();
        return new DetailsDTO(updatedDetails);
    }

    @Override
    @Transactional
    public void deleteDetails(Long id, Authentication authentication) {
        logger.info("Deleting details with id: {}", id);
        Long clientId = validateAuthentication(authentication);

        Details details = detailsRepository.findById(id)
                .filter(Details::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Details not found with id: " + id));
        validateOrderOwnership(clientId, details.getPurchaseOrder());

        // Revertir stock
        Product product = details.getProduct();
        product.setStock(product.getStock() + details.getQuantity());
        productRepository.save(product);

        details.setActive(false);
        detailsRepository.save(details);
        searchRepository.save(details);
        redisTemplate.delete(DETAILS_KEY_PREFIX + id);

        podStreamPrometheusConfig.incrementDetailsDeleted();
    }

    private Long validateAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementDetailsErrors();
            throw new SecurityException("Authentication required");
        }
        return Long.valueOf(authentication.getName());
    }

    private void validateOrderOwnership(Long clientId, PurchaseOrder purchaseOrder) {
        Authentication authentication = null;
        if (!purchaseOrder.getClient().getId().equals(clientId) && !authenticationHasRole(authentication, "ROLE_ADMIN")) {
            logger.warn("Client id: {} not authorized to access purchase order: {}", clientId, purchaseOrder.getId());
            podStreamPrometheusConfig.incrementDetailsErrors();
            throw new SecurityException("Not authorized to access this purchase order");
        }
    }

    private boolean authenticationHasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}