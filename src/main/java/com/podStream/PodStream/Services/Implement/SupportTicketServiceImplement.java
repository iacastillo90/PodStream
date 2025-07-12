package com.podStream.PodStream.Services.Implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.SupportTicketDTO;
import com.podStream.PodStream.Models.*;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.PurchaseOrderRepository;
import com.podStream.PodStream.Repositories.Jpa.SupportTicketRepository;
import com.podStream.PodStream.Repositories.Elastic.SupportTicketElasticRepository;
import com.podStream.PodStream.Services.OrderStatusHistoryService;
import com.podStream.PodStream.Services.SupportTicketService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar tickets de soporte en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@Service
public class SupportTicketServiceImplement implements SupportTicketService {

    private static final Logger logger = LoggerFactory.getLogger(SupportTicketServiceImplement.class);
    private static final String TICKET_KEY_PREFIX = "support_ticket:";
    private static final long TICKET_TTL_MINUTES = 60; // 1 hora

    private final SupportTicketRepository supportTicketRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupportTicketElasticRepository searchRepository;
    private final OrderStatusHistoryService historyService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${JIRA_URL}")
    private String jiraUrl;
    @Value("${JIRA_USERNAME}")
    private String jiraUsername;
    @Value("${JIRA_API_TOKEN}")
    private String jiraApiToken;
    @Value("${JIRA_PROJECT_KEY:POD}")
    private String jiraProjectKey;

    public SupportTicketServiceImplement(
            SupportTicketRepository supportTicketRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupportTicketElasticRepository searchRepository,
            OrderStatusHistoryService historyService,
            RedisTemplate<String, Object> redisTemplate,
            JavaMailSender mailSender,
            PodStreamPrometheusConfig podStreamPrometheusConfig,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.supportTicketRepository = supportTicketRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.searchRepository = searchRepository;
        this.historyService = historyService;
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public SupportTicketDTO createSupportTicket(SupportTicketDTO ticketDTO, Authentication authentication) {
        logger.info("Creating support ticket, title: {}", ticketDTO.getTitle());
        validateAuthentication(authentication, "ROLE_CLIENT");

        Long clientId = Long.valueOf(authentication.getName());
        Client client = validateClient(clientId);
        PurchaseOrder purchaseOrder = ticketDTO.getPurchaseOrderId() != null
                ? validatePurchaseOrder(ticketDTO.getPurchaseOrderId(), clientId)
                : null;

        SupportTicket ticket = new SupportTicket();
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setStatus(OrderStatus.PROCESSING);
        ticket.setCreatedBy(client);
        ticket.setPurchaseOrder(purchaseOrder);
        ticket.setActive(true);

        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        String jiraIssueKey = createJiraIssue(savedTicket);
        savedTicket.setJiraIssueKey(jiraIssueKey);
        supportTicketRepository.save(savedTicket);

        historyService.createHistory(savedTicket.getId(), null, OrderStatus.PROCESSING, authentication);
        searchRepository.save(savedTicket);
        redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + savedTicket.getId(), savedTicket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);

        sendSupportTicketEmail(savedTicket, "Support Ticket Creado");
        podStreamPrometheusConfig.incrementSupportTicketCreated();
        return new SupportTicketDTO(savedTicket);
    }

    @Override
    @Transactional
    public SupportTicketDTO updateSupportTicket(Long ticketId, SupportTicketDTO ticketDTO, Authentication authentication) {
        logger.info("Updating support ticket, ticketId: {}", ticketId);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .filter(SupportTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found: " + ticketId));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !ticket.getCreatedBy().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to update ticket: {}", clientId, ticketId);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new SecurityException("Not authorized to update this ticket");
        }

        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        PurchaseOrder purchaseOrder = ticketDTO.getPurchaseOrderId() != null
                ? validatePurchaseOrder(ticketDTO.getPurchaseOrderId(), clientId)
                : null;
        ticket.setPurchaseOrder(purchaseOrder);

        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        updateJiraIssue(updatedTicket);
        searchRepository.save(updatedTicket);
        redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + updatedTicket.getId(), updatedTicket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);

        sendSupportTicketEmail(updatedTicket, "Support Ticket Actualizado");
        podStreamPrometheusConfig.incrementSupportTicketUpdated();
        return new SupportTicketDTO(updatedTicket);
    }

    @Override
    @Transactional
    public SupportTicketDTO updateTicketStatus(Long ticketId, OrderStatus newStatus, String changedBy, Authentication authentication) {
        logger.info("Updating support ticket status, ticketId: {}, newStatus: {}", ticketId, newStatus);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .filter(SupportTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found: " + ticketId));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !ticket.getCreatedBy().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to update ticket: {}", clientId, ticketId);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new SecurityException("Not authorized to update this ticket");
        }

        ticket.setStatus(newStatus);
        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        historyService.createHistory(updatedTicket.getId(), null, newStatus, authentication);
        updateJiraIssue(updatedTicket);
        searchRepository.save(updatedTicket);
        redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + updatedTicket.getId(), updatedTicket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);

        sendSupportTicketEmail(updatedTicket, "Actualización de Estado del Ticket");
        podStreamPrometheusConfig.incrementSupportTicketUpdated();
        return new SupportTicketDTO(updatedTicket);
    }

    @Override
    public SupportTicketDTO getSupportTicket(Long ticketId, Authentication authentication) {
        logger.info("Fetching support ticket, ticketId: {}", ticketId);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        String cacheKey = TICKET_KEY_PREFIX + ticketId;
        SupportTicket cachedTicket = (SupportTicket) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTicket != null && cachedTicket.isActive()) {
            logger.info("Support ticket id: {} retrieved from cache", ticketId);
            podStreamPrometheusConfig.incrementSupportTicketCacheHit();
            return new SupportTicketDTO(cachedTicket);
        }

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .filter(SupportTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found: " + ticketId));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !ticket.getCreatedBy().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to view ticket: {}", clientId, ticketId);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new SecurityException("Not authorized to view this ticket");
        }

        redisTemplate.opsForValue().set(cacheKey, ticket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementSupportTicketFetched();
        return new SupportTicketDTO(ticket);
    }

    @Override
    public List<SupportTicketDTO> getSupportTicketsByClient(Authentication authentication) {
        logger.info("Fetching support tickets for client");
        Long clientId = validateAuthentication(authentication, "ROLE_CLIENT");

        List<SupportTicket> tickets = supportTicketRepository.findByCreatedByIdAndActiveTrue(clientId);
        podStreamPrometheusConfig.incrementSupportTicketFetched();
        return tickets.stream()
                .map(ticket -> {
                    redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + ticket.getId(), ticket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
                    return new SupportTicketDTO(ticket);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SupportTicketDTO> getSupportTicketsByPurchaseOrder(Long purchaseOrderId, Authentication authentication) {
        logger.info("Fetching support tickets for purchase order: {}", purchaseOrderId);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            PurchaseOrder purchaseOrder = validatePurchaseOrder(purchaseOrderId, clientId);
        }

        List<SupportTicket> tickets = supportTicketRepository.findByPurchaseOrderIdAndActiveTrue(purchaseOrderId);
        podStreamPrometheusConfig.incrementSupportTicketFetched();
        return tickets.stream()
                .map(ticket -> {
                    redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + ticket.getId(), ticket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
                    return new SupportTicketDTO(ticket);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSupportTicket(Long ticketId, Authentication authentication) {
        logger.info("Deleting support ticket, ticketId: {}", ticketId);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .filter(SupportTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found: " + ticketId));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !ticket.getCreatedBy().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to delete ticket: {}", clientId, ticketId);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new SecurityException("Not authorized to delete this ticket");
        }

        ticket.setActive(false);
        supportTicketRepository.save(ticket);
        searchRepository.save(ticket);
        redisTemplate.delete(TICKET_KEY_PREFIX + ticketId);
        podStreamPrometheusConfig.incrementSupportTicketDeleted();
    }

    private String createJiraIssue(SupportTicket ticket) {
        String url = jiraUrl + "/rest/api/3/issue";
        HttpHeaders headers = new HttpHeaders();
        String auth = jiraUsername + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.add("Authorization", "Basic " + encodedAuth);
        headers.add("Content-Type", "application/json");

        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", jiraProjectKey));
        fields.put("summary", "Ticket #" + ticket.getId() + ": " + ticket.getTitle());
        fields.put("issuetype", Map.of("name", "Task"));
        fields.put("description", Map.of(
                "type", "doc",
                "version", 1,
                "content", List.of(
                        Map.of("type", "paragraph",
                                "content", List.of(
                                        Map.of("type", "text", "text", ticket.getDescription())
                                ))
                )
        ));

        Map<String, Object> bodyMap = Map.of("fields", fields);

        String body;
        try {
            body = objectMapper.writeValueAsString(bodyMap);
        } catch (Exception e) {
            logger.error("Error serializing JSON body for Jira: {}", e.getMessage());
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new RuntimeException("Error serializing Jira request", e);
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            logger.info("Jira issue created for ticket #{}: {}", ticket.getId(), response.getBody());
            podStreamPrometheusConfig.incrementSupportTicketJiraCreated();
            return (String) response.getBody().get("key");
        } catch (Exception e) {
            logger.error("Error creating Jira issue for ticket #{}", ticket.getId(), e);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new RuntimeException("Error creating Jira issue", e);
        }
    }

    private void updateJiraIssue(SupportTicket ticket) {
        if (ticket.getJiraIssueKey() == null) {
            logger.warn("No Jira issue key found for ticket #{}", ticket.getId());
            return;
        }

        String url = jiraUrl + "/rest/api/3/issue/" + ticket.getJiraIssueKey();
        HttpHeaders headers = new HttpHeaders();
        String auth = jiraUsername + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.add("Authorization", "Basic " + encodedAuth);
        headers.add("Content-Type", "application/json");

        Map<String, Object> fields = new HashMap<>();
        fields.put("summary", "Ticket #" + ticket.getId() + ": " + ticket.getTitle());
        fields.put("description", Map.of(
                "type", "doc",
                "version", 1,
                "content", List.of(
                        Map.of("type", "paragraph",
                                "content", List.of(
                                        Map.of("type", "text", "text", ticket.getDescription() + "\nStatus: " + ticket.getStatus().getDescription())
                                ))
                )
        ));

        Map<String, Object> bodyMap = Map.of("fields", fields);

        String body;
        try {
            body = objectMapper.writeValueAsString(bodyMap);
        } catch (Exception e) {
            logger.error("Error serializing JSON body for Jira update: {}", e.getMessage());
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new RuntimeException("Error serializing Jira update request", e);
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            logger.info("Jira issue updated for ticket #{}", ticket.getId());
            podStreamPrometheusConfig.incrementSupportTicketJiraUpdated();
        } catch (Exception e) {
            logger.error("Error updating Jira issue for ticket #{}", ticket.getId(), e);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new RuntimeException("Error updating Jira issue", e);
        }
    }

    private void sendSupportTicketEmail(SupportTicket ticket, String subject) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String email = ticket.getCreatedBy().getEmail();
            validateEmail(email, ticket.getId());
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText("Ticket ID: " + ticket.getId() + "\nTítulo: " + ticket.getTitle() + "\nDescripción: " + ticket.getDescription() + "\nEstado: " + ticket.getStatus().getDescription());
            mailSender.send(message);
            podStreamPrometheusConfig.incrementEmailSent();
        } catch (MessagingException e) {
            logger.error("Error sending email for support ticket #{}", ticket.getId(), e);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new RuntimeException("Error sending support ticket email", e);
        }
    }

    private Client validateClient(Long clientId) {
        return supportTicketRepository.findById(clientId)
                .filter(Client::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId)).getCreatedBy();
    }

    private PurchaseOrder validatePurchaseOrder(Long purchaseOrderId, Long clientId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .filter(PurchaseOrder::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found: " + purchaseOrderId));
        if (!purchaseOrder.getClient().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to access purchase order: {}", clientId, purchaseOrderId);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new SecurityException("Not authorized to access this purchase order");
        }
        return purchaseOrder;
    }

    private void validateEmail(String email, Long ticketId) {
        if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            logger.warn("Invalid or empty email for support ticket #{}", ticketId);
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new IllegalArgumentException("Invalid or empty client email");
        }
    }

    private Long validateAuthentication(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new SecurityException("Authentication required");
        }
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> List.of(roles).contains(auth.getAuthority()));
        if (!hasRole) {
            logger.warn("User {} does not have required roles: {}", authentication.getName(), String.join(", ", roles));
            podStreamPrometheusConfig.incrementSupportTicketErrors();
            throw new SecurityException("Insufficient permissions");
        }
        return Long.valueOf(authentication.getName());
    }
}