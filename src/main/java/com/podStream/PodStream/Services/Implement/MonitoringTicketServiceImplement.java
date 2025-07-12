package com.podStream.PodStream.Services.Implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.MonitoringTicketDTO;
import com.podStream.PodStream.DTOS.MonitoringTicketRequestDTO;
import com.podStream.PodStream.Models.MonitoringTicket;
import com.podStream.PodStream.Models.TicketHistory;
import com.podStream.PodStream.Models.TicketStatus;
import com.podStream.PodStream.Repositories.Jpa.MonitoringTicketRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticMonitoringTicketRepository;
import com.podStream.PodStream.Services.MonitoringTicketService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar tickets de monitoreo.
 */
@Service
public class MonitoringTicketServiceImplement implements MonitoringTicketService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringTicketServiceImplement.class);
    private static final String TICKET_KEY_PREFIX = "monitoring-ticket:active:";
    private static final long TICKET_TTL_MINUTES = 1440; // 1 día

    @Autowired
    private MonitoringTicketRepository ticketRepository;
    @Autowired
    private ElasticMonitoringTicketRepository searchRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private PodStreamPrometheusConfig podStreamPrometheusConfig;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${JIRA_URL}")
    private String jiraUrl;
    @Value("${JIRA_USERNAME}")
    private String jiraUsername;
    @Value("${JIRA_API_TOKEN}")
    private String jiraApiToken;
    @Value("${JIRA_PROJECT_KEY:POD}")
    private String jiraProjectKey;



    @Override
    @Transactional
    public MonitoringTicketDTO createTicket(MonitoringTicketRequestDTO request, Authentication authentication) {
        logger.info("Creating monitoring ticket: {}", request.getTitle());
        validateAuthentication(authentication, "ROLE_ADMIN");

        MonitoringTicket ticket = new MonitoringTicket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setTicketStatus(request.getTicketStatus());
        ticket.setSource(request.getSource());
        ticket.setErrorCode(request.getErrorCode());
        ticket.setSeverity(request.getSeverity());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setActive(true);

        TicketHistory history = new TicketHistory();
        history.setMonitoringTicket(ticket);
        history.setOldStatus(null);
        history.setNewStatus(request.getTicketStatus());
        ticket.getHistory().add(history);

        MonitoringTicket savedTicket = ticketRepository.save(ticket);
        searchRepository.save(savedTicket);

        String jiraIssueId = createJiraIssue(savedTicket);
        savedTicket.setJiraIssueId(jiraIssueId);
        ticketRepository.save(savedTicket);

        redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + savedTicket.getId(), savedTicket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
        sendEmail(savedTicket, "Ticket Creado");

        podStreamPrometheusConfig.incrementMonitoringTicketCreated();
        return new MonitoringTicketDTO(savedTicket);
    }

    @Override
    public MonitoringTicketDTO getTicket(Long id, Authentication authentication) {
        logger.info("Fetching monitoring ticket with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        String cacheKey = TICKET_KEY_PREFIX + id;
        MonitoringTicket cachedTicket = (MonitoringTicket) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTicket != null && cachedTicket.isActive()) {
            logger.info("Ticket id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementMonitoringTicketCacheHit();
            return new MonitoringTicketDTO(cachedTicket);
        }

        MonitoringTicket ticket = ticketRepository.findById(id)
                .filter(MonitoringTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Monitoring ticket not found with id: " + id));

        redisTemplate.opsForValue().set(cacheKey, ticket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementMonitoringTicketFetched();
        return new MonitoringTicketDTO(ticket);
    }

    @Override
    public List<MonitoringTicketDTO> getTicketsBySource(String source, Authentication authentication) {
        logger.info("Fetching monitoring tickets by source: {}", source);
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        List<MonitoringTicket> tickets = ticketRepository.findBySourceAndActiveTrue(source);
        podStreamPrometheusConfig.incrementMonitoringTicketFetched();
        return tickets.stream()
                .map(ticket -> {
                    redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + ticket.getId(), ticket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
                    return new MonitoringTicketDTO(ticket);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MonitoringTicketDTO> getTicketsBySeverity(String severity, Authentication authentication) {
        logger.info("Fetching monitoring tickets by severity: {}", severity);
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        List<MonitoringTicket> tickets = ticketRepository.findBySeverityAndActiveTrue(severity);
        podStreamPrometheusConfig.incrementMonitoringTicketFetched();
        return tickets.stream()
                .map(ticket -> {
                    redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + ticket.getId(), ticket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
                    return new MonitoringTicketDTO(ticket);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MonitoringTicketDTO> getTicketsByStatus(TicketStatus status, Authentication authentication) {
        logger.info("Fetching monitoring tickets by status: {}", status);
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        List<MonitoringTicket> tickets = ticketRepository.findByTicketStatusAndActiveTrue(status);
        podStreamPrometheusConfig.incrementMonitoringTicketFetched();
        return tickets.stream()
                .map(ticket -> {
                    redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + ticket.getId(), ticket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
                    return new MonitoringTicketDTO(ticket);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MonitoringTicketDTO> getAllTickets(Authentication authentication) {
        logger.info("Fetching all monitoring tickets");
        validateAuthentication(authentication, "ROLE_ADMIN", "ROLE_DEVELOPER");

        List<MonitoringTicket> tickets = ticketRepository.findByActiveTrue();
        podStreamPrometheusConfig.incrementMonitoringTicketFetched();
        return tickets.stream()
                .map(ticket -> {
                    redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + ticket.getId(), ticket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
                    return new MonitoringTicketDTO(ticket);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MonitoringTicketDTO updateTicket(Long id, MonitoringTicketRequestDTO request, Authentication authentication) {
        logger.info("Updating monitoring ticket with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN");

        MonitoringTicket ticket = ticketRepository.findById(id)
                .filter(MonitoringTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Monitoring ticket not found with id: " + id));

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setSource(request.getSource());
        ticket.setErrorCode(request.getErrorCode());
        ticket.setSeverity(request.getSeverity());

        MonitoringTicket updatedTicket = ticketRepository.save(ticket);
        searchRepository.save(updatedTicket);
        updateJiraIssue(updatedTicket);
        redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + updatedTicket.getId(), updatedTicket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
        sendEmail(updatedTicket, "Ticket Actualizado");

        podStreamPrometheusConfig.incrementMonitoringTicketUpdated();
        return new MonitoringTicketDTO(updatedTicket);
    }

    @Override
    @Transactional
    public MonitoringTicketDTO updateStatus(Long id, TicketStatus status, Authentication authentication) {
        logger.info("Updating status of monitoring ticket id: {} to {}", id, status);
        validateAuthentication(authentication, "ROLE_ADMIN");

        MonitoringTicket ticket = ticketRepository.findById(id)
                .filter(MonitoringTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Monitoring ticket not found with id: " + id));

        TicketHistory history = new TicketHistory();
        history.setMonitoringTicket(ticket);
        history.setOldStatus(ticket.getTicketStatus());
        history.setNewStatus(status);
        ticket.getHistory().add(history);
        ticket.setTicketStatus(status);

        MonitoringTicket updatedTicket = ticketRepository.save(ticket);
        searchRepository.save(updatedTicket);
        updateJiraIssue(updatedTicket);
        redisTemplate.opsForValue().set(TICKET_KEY_PREFIX + updatedTicket.getId(), updatedTicket, TICKET_TTL_MINUTES, TimeUnit.MINUTES);
        sendEmail(updatedTicket, "Ticket Actualizado a " + status.getDescription());

        podStreamPrometheusConfig.incrementMonitoringTicketUpdated();
        return new MonitoringTicketDTO(updatedTicket);
    }

    @Override
    @Transactional
    public void deleteTicket(Long id, Authentication authentication) {
        logger.info("Deleting monitoring ticket with id: {}", id);
        validateAuthentication(authentication, "ROLE_ADMIN");

        MonitoringTicket ticket = ticketRepository.findById(id)
                .filter(MonitoringTicket::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Monitoring ticket not found with id: " + id));

        ticket.setActive(false);
        ticketRepository.save(ticket);
        searchRepository.save(ticket);
        redisTemplate.delete(TICKET_KEY_PREFIX + id);

        podStreamPrometheusConfig.incrementMonitoringTicketDeleted();
    }

    private String createJiraIssue(MonitoringTicket ticket) {
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
                                        Map.of("type", "text", "text", ticket.getDescription() + "\n\nSource: " + ticket.getSource() + "\nError Code: " + ticket.getErrorCode() + "\nSeverity: " + ticket.getSeverity())
                                ))
                )
        ));

        Map<String, Object> bodyMap = Map.of("fields", fields);

        String body;
        try {
            body = objectMapper.writeValueAsString(bodyMap);
        } catch (Exception e) {
            logger.error("Error serializing JSON body for Jira: {}", e.getMessage());
            podStreamPrometheusConfig.incrementMonitoringTicketErrors();
            throw new RuntimeException("Failed to serialize Jira request body", e);
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            String jiraIssueId = (String) responseBody.get("id");
            logger.info("Jira issue created: {}", jiraIssueId);
            podStreamPrometheusConfig.incrementJiraIssuesCreated();
            return jiraIssueId;
        } catch (Exception e) {
            logger.error("Error creating Jira issue for ticket #{}", ticket.getId(), e);
            podStreamPrometheusConfig.incrementMonitoringTicketErrors();
            throw new RuntimeException("Failed to create Jira issue", e);
        }
    }

    private void updateJiraIssue(MonitoringTicket ticket) {
        if (ticket.getJiraIssueId() == null) {
            logger.warn("No Jira issue ID found for ticket #{}", ticket.getId());
            return;
        }

        String url = jiraUrl + "/rest/api/3/issue/" + ticket.getJiraIssueId();
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
                                        Map.of("type", "text", "text", ticket.getDescription() + "\n\nSource: " + ticket.getSource() + "\nError Code: " + ticket.getErrorCode() + "\nSeverity: " + ticket.getSeverity() + "\nStatus: " + ticket.getTicketStatus().getDescription())
                                ))
                )
        ));

        Map<String, Object> bodyMap = Map.of("fields", fields);

        String body;
        try {
            body = objectMapper.writeValueAsString(bodyMap);
        } catch (Exception e) {
            logger.error("Error serializing JSON body for Jira update: {}", e.getMessage());
            podStreamPrometheusConfig.incrementMonitoringTicketErrors();
            throw new RuntimeException("Failed to serialize Jira update request body", e);
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            logger.info("Jira issue updated for ticket #{}", ticket.getId());
            podStreamPrometheusConfig.incrementJiraIssuesUpdated();
        } catch (Exception e) {
            logger.error("Error updating Jira issue for ticket #{}", ticket.getId(), e);
            podStreamPrometheusConfig.incrementMonitoringTicketErrors();
            throw new RuntimeException("Failed to update Jira issue", e);
        }
    }

    private void sendEmail(MonitoringTicket ticket, String subject) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo("podstreamstore@gmail.com"); // Reemplazar con email configurado
            helper.setSubject(subject);
            helper.setText("Ticket ID: " + ticket.getId() + "\nTitle: " + ticket.getTitle() + "\nDescription: " + ticket.getDescription() +
                    "\nStatus: " + ticket.getTicketStatus().getDescription() + "\nSource: " + ticket.getSource() +
                    "\nError Code: " + ticket.getErrorCode() + "\nSeverity: " + ticket.getSeverity());
            mailSender.send(message);
            podStreamPrometheusConfig.incrementEmailSent();
        } catch (MessagingException e) {
            logger.error("Error sending email for ticket #{}", ticket.getId(), e);
            podStreamPrometheusConfig.incrementEmailErrors();
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private void validateAuthentication(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementMonitoringTicketErrors();
            throw new SecurityException("Authentication required");
        }
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> List.of(roles).contains(auth.getAuthority()));
        if (!hasRole) {
            logger.warn("User {} does not have required roles: {}", authentication.getName(), String.join(", ", roles));
            podStreamPrometheusConfig.incrementMonitoringTicketErrors();
            throw new SecurityException("Insufficient permissions");
        }
    }
}