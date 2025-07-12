package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.TicketHistoryDTO;
import com.podStream.PodStream.Models.MonitoringTicket;
import com.podStream.PodStream.Models.TicketHistory;
import com.podStream.PodStream.Repositories.Jpa.MonitoringTicketRepository;
import com.podStream.PodStream.Repositories.Jpa.TicketHistoryRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticTicketHistoryRepository;
import com.podStream.PodStream.Services.TicketHistoryService;
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
 * Implementación del servicio para gestionar el historial de tickets de monitoreo en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@Service
public class TicketHistoryServiceImplement implements TicketHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(TicketHistoryServiceImplement.class);
    private static final String HISTORY_KEY_PREFIX = "ticket_history:";
    private static final long HISTORY_TTL_MINUTES = 60; // 1 hora

    private final TicketHistoryRepository ticketHistoryRepository;
    private final MonitoringTicketRepository monitoringTicketRepository;
    private final ElasticTicketHistoryRepository searchRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public TicketHistoryServiceImplement(
            TicketHistoryRepository ticketHistoryRepository,
            MonitoringTicketRepository monitoringTicketRepository,
            ElasticTicketHistoryRepository searchRepository,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.monitoringTicketRepository = monitoringTicketRepository;
        this.searchRepository = searchRepository;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    @Override
    @Transactional
    public TicketHistoryDTO createTicketHistory(TicketHistoryDTO ticketHistoryDTO, Authentication authentication) {
        logger.info("Creating ticket history for monitoring ticket: {}", ticketHistoryDTO.getMonitoringTicketId());
        validateAuthentication(authentication, "ROLE_ADMIN");

        Long changedById = Long.valueOf(authentication.getName());
        MonitoringTicket monitoringTicket = validateMonitoringTicket(ticketHistoryDTO.getMonitoringTicketId());

        TicketHistory history = new TicketHistory();
        history.setMonitoringTicket(monitoringTicket);
        history.setOldStatus(ticketHistoryDTO.getOldStatus());
        history.setNewStatus(ticketHistoryDTO.getNewStatus());
        history.setChangedById(changedById);
        history.setActive(true);

        TicketHistory savedHistory = ticketHistoryRepository.save(history);
        searchRepository.save(savedHistory);
        redisTemplate.opsForValue().set(HISTORY_KEY_PREFIX + savedHistory.getId(), savedHistory, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementTicketHistoryCreated();
        return new TicketHistoryDTO(savedHistory);
    }

    @Override
    public TicketHistoryDTO getTicketHistory(Long historyId, Authentication authentication) {
        logger.info("Fetching ticket history, historyId: {}", historyId);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        String cacheKey = HISTORY_KEY_PREFIX + historyId;
        TicketHistory cachedHistory = (TicketHistory) redisTemplate.opsForValue().get(cacheKey);
        if (cachedHistory != null && cachedHistory.isActive()) {
            logger.info("Ticket history id: {} retrieved from cache", historyId);
            podStreamPrometheusConfig.incrementTicketHistoryCacheHit();
            return new TicketHistoryDTO(cachedHistory);
        }

        TicketHistory history = ticketHistoryRepository.findById(historyId)
                .filter(TicketHistory::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Ticket history not found: " + historyId));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !history.getMonitoringTicket().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to view history: {}", clientId, historyId);
            podStreamPrometheusConfig.incrementTicketHistoryErrors();
            throw new SecurityException("Not authorized to view this ticket history");
        }

        redisTemplate.opsForValue().set(cacheKey, history, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementTicketHistoryFetched();
        return new TicketHistoryDTO(history);
    }

    @Override
    public List<TicketHistoryDTO> getTicketHistoryByMonitoringTicket(Long monitoringTicketId, Authentication authentication) {
        logger.info("Fetching ticket history for monitoring ticket: {}", monitoringTicketId);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        MonitoringTicket monitoringTicket = validateMonitoringTicket(monitoringTicketId);
        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !monitoringTicket.getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to view ticket: {}", clientId, monitoringTicketId);
            podStreamPrometheusConfig.incrementTicketHistoryErrors();
            throw new SecurityException("Not authorized to view this ticket history");
        }

        List<TicketHistory> historyList = ticketHistoryRepository.findByMonitoringTicketIdAndActiveTrue(monitoringTicketId);
        podStreamPrometheusConfig.incrementTicketHistoryFetched();
        return historyList.stream()
                .map(history -> {
                    redisTemplate.opsForValue().set(HISTORY_KEY_PREFIX + history.getId(), history, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);
                    return new TicketHistoryDTO(history);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTicketHistory(Long historyId, Authentication authentication) {
        logger.info("Deleting ticket history, historyId: {}", historyId);
        validateAuthentication(authentication, "ROLE_ADMIN");

        TicketHistory history = ticketHistoryRepository.findById(historyId)
                .filter(TicketHistory::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Ticket history not found: " + historyId));

        history.setActive(false);
        ticketHistoryRepository.save(history);
        searchRepository.save(history);
        redisTemplate.delete(HISTORY_KEY_PREFIX + historyId);
        podStreamPrometheusConfig.incrementTicketHistoryDeleted();
    }

    private MonitoringTicket validateMonitoringTicket(Long monitoringTicketId) {
        return monitoringTicketRepository.findById(monitoringTicketId)
                .filter(MonitoringTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Monitoring ticket not found: " + monitoringTicketId));
    }

    private Long validateAuthentication(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementTicketHistoryErrors();
            throw new SecurityException("Authentication required");
        }
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> List.of(roles).contains(auth.getAuthority()));
        if (!hasRole) {
            logger.warn("User {} does not have required roles: {}", authentication.getName(), String.join(", ", roles));
            podStreamPrometheusConfig.incrementTicketHistoryErrors();
            throw new SecurityException("Insufficient permissions");
        }
        return Long.valueOf(authentication.getName());
    }
}