package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.OrderStatusHistoryDTO;
import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.OrderStatusHistory;
import com.podStream.PodStream.Models.PurchaseOrder;
import com.podStream.PodStream.Models.SupportTicket;
import com.podStream.PodStream.Repositories.Jpa.OrderStatusHistoryRepository;
import com.podStream.PodStream.Repositories.Elastic.OrderStatusHistoryElasticRepository;
import com.podStream.PodStream.Repositories.Jpa.PurchaseOrderRepository;
import com.podStream.PodStream.Repositories.Jpa.SupportTicketRepository;
import com.podStream.PodStream.Services.OrderStatusHistoryService;
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

/**
 * Implementación del servicio para gestionar el historial de cambios de estado de órdenes.
 */
@Service
public class OrderStatusHistoryServiceImplement implements OrderStatusHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusHistoryServiceImplement.class);
    private static final String HISTORY_KEY_PREFIX = "order-status-history:active:";
    private static final long HISTORY_TTL_MINUTES = 1440; // 1 día

    private final OrderStatusHistoryRepository historyRepository;
    private final OrderStatusHistoryElasticRepository searchRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public OrderStatusHistoryServiceImplement(
            OrderStatusHistoryRepository historyRepository,
            OrderStatusHistoryElasticRepository searchRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupportTicketRepository supportTicketRepository,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.historyRepository = historyRepository;
        this.searchRepository = searchRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    @Override
    @Transactional
    public OrderStatusHistoryDTO createHistory(Long purchaseOrderId, Long supportTicketId, OrderStatus newStatus, Authentication authentication) {
        logger.info("Creating order status history for purchase order: {}", purchaseOrderId);
        validateAuthentication(authentication, "ROLE_ADMIN");

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .filter(PurchaseOrder::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found: " + purchaseOrderId));

        SupportTicket supportTicket = null;
        if (supportTicketId != null) {
            supportTicket = supportTicketRepository.findById(supportTicketId)
                    .orElseThrow(() -> new EntityNotFoundException("Support ticket not found: " + supportTicketId));
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setPurchaseOrder(purchaseOrder);
        history.setSupportTicket(supportTicket);
        history.setOldStatus(purchaseOrder.getStatus());
        history.setNewStatus(newStatus);
        history.setChangedBy(authentication.getName());
        history.setChangeDate(LocalDateTime.now());
        history.setActive(true);

        OrderStatusHistory savedHistory = historyRepository.save(history);
        searchRepository.save(savedHistory);
        redisTemplate.opsForValue().set(HISTORY_KEY_PREFIX + savedHistory.getId(), savedHistory, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementOrderStatusHistoryCreated();
        return new OrderStatusHistoryDTO(savedHistory);
    }

    @Override
    public OrderStatusHistoryDTO getHistory(Long id, Authentication authentication) {
        logger.info("Fetching order status history with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        String cacheKey = HISTORY_KEY_PREFIX + id;
        OrderStatusHistory cachedHistory = (OrderStatusHistory) redisTemplate.opsForValue().get(cacheKey);
        if (cachedHistory != null && cachedHistory.isActive()) {
            logger.info("Order status history id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementOrderStatusHistoryCacheHit();
            return new OrderStatusHistoryDTO(cachedHistory);
        }

        OrderStatusHistory history = historyRepository.findById(id)
                .filter(OrderStatusHistory::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Order status history not found with id: " + id));

        redisTemplate.opsForValue().set(cacheKey, history, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementOrderStatusHistoryFetched();
        return new OrderStatusHistoryDTO(history);
    }

    @Override
    public List<OrderStatusHistoryDTO> getHistoriesByPurchaseOrder(Long purchaseOrderId, Authentication authentication) {
        logger.info("Fetching order status histories for purchase order: {}", purchaseOrderId);
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        List<OrderStatusHistory> histories = historyRepository.findByPurchaseOrderIdAndActiveTrue(purchaseOrderId);
        podStreamPrometheusConfig.incrementOrderStatusHistoryFetched();
        return histories.stream()
                .map(history -> {
                    redisTemplate.opsForValue().set(HISTORY_KEY_PREFIX + history.getId(), history, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);
                    return new OrderStatusHistoryDTO(history);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderStatusHistoryDTO> getHistoriesByNewStatus(OrderStatus newStatus, Authentication authentication) {
        logger.info("Fetching order status histories by new status: {}", newStatus);
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        List<OrderStatusHistory> histories = historyRepository.findByNewStatusAndActiveTrue(newStatus);
        podStreamPrometheusConfig.incrementOrderStatusHistoryFetched();
        return histories.stream()
                .map(history -> {
                    redisTemplate.opsForValue().set(HISTORY_KEY_PREFIX + history.getId(), history, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);
                    return new OrderStatusHistoryDTO(history);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderStatusHistoryDTO> getHistoriesByChangedBy(String changedBy, Authentication authentication) {
        logger.info("Fetching order status histories by changedBy: {}", changedBy);
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        List<OrderStatusHistory> histories = historyRepository.findByChangedByAndActiveTrue(changedBy);
        podStreamPrometheusConfig.incrementOrderStatusHistoryFetched();
        return histories.stream()
                .map(history -> {
                    redisTemplate.opsForValue().set(HISTORY_KEY_PREFIX + history.getId(), history, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);
                    return new OrderStatusHistoryDTO(history);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderStatusHistoryDTO> getAllHistories(Authentication authentication) {
        logger.info("Fetching all order status histories");
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        List<OrderStatusHistory> histories = historyRepository.findByActiveTrue();
        podStreamPrometheusConfig.incrementOrderStatusHistoryFetched();
        return histories.stream()
                .map(history -> {
                    redisTemplate.opsForValue().set(HISTORY_KEY_PREFIX + history.getId(), history, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);
                    return new OrderStatusHistoryDTO(history);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteHistory(Long id, Authentication authentication) {
        logger.info("Deleting order status history with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN");

        OrderStatusHistory history = historyRepository.findById(id)
                .filter(OrderStatusHistory::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Order status history not found with id: " + id));

        history.setActive(false);
        historyRepository.save(history);
        searchRepository.save(history);
        redisTemplate.delete(HISTORY_KEY_PREFIX + id);

        podStreamPrometheusConfig.incrementOrderStatusHistoryDeleted();
    }

    private void validateAuthentication(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementOrderStatusHistoryErrors();
            throw new SecurityException("Authentication required");
        }
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> List.of(roles).contains(auth.getAuthority()));
        if (!hasRole) {
            logger.warn("User {} does not have required roles: {}", authentication.getName(), String.join(", ", roles));
            podStreamPrometheusConfig.incrementOrderStatusHistoryErrors();
            throw new SecurityException("Insufficient permissions");
        }
    }
}