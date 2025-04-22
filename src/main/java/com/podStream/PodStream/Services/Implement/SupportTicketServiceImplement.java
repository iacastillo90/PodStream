package com.podStream.PodStream.Services.Implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.podStream.PodStream.Models.SupportTicket;
import com.podStream.PodStream.Models.User.User;
import com.podStream.PodStream.Repositories.SupportTicketRepository;
import com.podStream.PodStream.Services.SupportTicketService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupportTicketServiceImplement implements SupportTicketService {
    @Autowired
    private SupportTicketRepository supportTicketRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MeterRegistry meterRegistry;

    @Value("${jira.url}")
    private String jiraUrl;
    @Value("${jira.username}")
    private String jiraUsername;
    @Value("${jira.api-token}")
    private String jiraApiToken;
    @Value("${jira.project-key:POD}")
    private String jiraProjectKey;

    private static final Logger logger = LoggerFactory.getLogger(SupportTicketServiceImplement.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SupportTicket createSupportTicket(SupportTicket ticket) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            ticket.setCreatedBy((User) authentication.getPrincipal());
        }

        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        logger.info("Support Ticket creado: ID={}, Estado={}", savedTicket.getId(), savedTicket.getOrderStatus());

        createJiraIssue(savedTicket);
        sendEmail(savedTicket, null, "Support Ticket Creado");
        return savedTicket;
    }

    private void createJiraIssue(SupportTicket ticket) {
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

        Map<String, Object> description = new HashMap<>();
        description.put("type", "doc");
        description.put("version", 1);
        description.put("content", List.of(
                Map.of("type", "paragraph",
                        "content", List.of(
                                Map.of("type", "text", "text", ticket.getDescription())
                        ))
        ));
        fields.put("description", description);

        Map<String, Object> bodyMap = Map.of("fields", fields);

        String body;
        try {
            body = objectMapper.writeValueAsString(bodyMap);
        } catch (Exception e) {
            logger.error("Error al serializar el cuerpo JSON para Jira: {}", e.getMessage());
            meterRegistry.counter("jira.serialization.errors").increment();
            return;
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            logger.info("Issue creado en Jira: {}", response.getBody());
            meterRegistry.counter("jira.issues.created").increment();
        } catch (Exception e) {
            logger.error("Error al crear issue en Jira para el ticket #{}", ticket.getId(), e);
            meterRegistry.counter("jira.issues.errors").increment();
            throw new RuntimeException("No se pudo crear el issue en Jira", e);
        }
    }

    private void sendEmail(SupportTicket ticket, byte[] pdfBytes, String subject) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo("podstreamstore@gmail.com"); // Reemplazar con email real
            helper.setSubject(subject);
            helper.setText("Ticket ID: " + ticket.getId() + "\nStatus: " + ticket.getOrderStatus());
            if (pdfBytes != null) {
                helper.addAttachment("factura.pdf", new ByteArrayResource(pdfBytes));
            }
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Error al enviar correo para el ticket #{}", ticket.getId(), e);
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}