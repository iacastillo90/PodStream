package com.podStream.PodStream.Services.Implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.podStream.PodStream.Models.MonitoringTicket;
import com.podStream.PodStream.Models.TicketHistory;
import com.podStream.PodStream.Models.TicketStatus;
import com.podStream.PodStream.Repositories.TicketRepository;
import com.podStream.PodStream.Services.TicketService;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketServiceImplement implements TicketService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MeterRegistry meterRegistry;  // Inyecta MeterRegistry

    @Value("${jira.url}")
    private String jiraUrl;
    @Value("${jira.username}")
    private String jiraUsername;
    @Value("${jira.api-token}")
    private String jiraApiToken;
    @Value("${jira.project-key:POD}")
    private String jiraProjectKey;

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImplement.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();  // Para serializar JSON

    public MonitoringTicket createTicket(MonitoringTicket monitoringTicket) {

        // Guardar el ticket en la base de datos
        MonitoringTicket savedMonitoringTicket = ticketRepository.save(monitoringTicket);
        logger.info("Ticket creado: ID={}, Estado={}", savedMonitoringTicket.getId(), savedMonitoringTicket.getTicketStatus());

        // Crear un issue en Jira
        createJiraIssue(savedMonitoringTicket);

        // Enviar correo
        sendEmail(savedMonitoringTicket, null, "Ticket Creado");
        return savedMonitoringTicket;
    }

    private void createJiraIssue(MonitoringTicket monitoringTicket) {
        String url = jiraUrl + "/rest/api/3/issue";
        HttpHeaders headers = new HttpHeaders();
        String auth = jiraUsername + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.add("Authorization", "Basic " + encodedAuth);
        headers.add("Content-Type", "application/json");

        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", jiraProjectKey));
        fields.put("summary", "Ticket #" + monitoringTicket.getId() + ": " + monitoringTicket.getTitle());
        fields.put("issuetype", Map.of("name", "Task"));

        Map<String, Object> description = new HashMap<>();
        description.put("type", "doc");
        description.put("version", 1);
        description.put("content", List.of(
                Map.of("type", "paragraph",
                        "content", List.of(
                                Map.of("type", "text", "text", monitoringTicket.getDescription())
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
            logger.error("Error al crear issue en Jira para el ticket #{}", monitoringTicket.getId(), e);
            meterRegistry.counter("jira.issues.errors").increment();
            throw new RuntimeException("No se pudo crear el issue en Jira", e);
        }
    }

    public List<MonitoringTicket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public MonitoringTicket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    public MonitoringTicket updateTicket(MonitoringTicket monitoringTicket) {
        MonitoringTicket existingMonitoringTicket = getTicketById(monitoringTicket.getId());
        existingMonitoringTicket.setTitle(monitoringTicket.getTitle());
        existingMonitoringTicket.setDescription(monitoringTicket.getDescription());
        existingMonitoringTicket.setTicketStatus(monitoringTicket.getTicketStatus());
        return ticketRepository.save(existingMonitoringTicket);
    }

    public MonitoringTicket updateStatus(Long id, TicketStatus status) {
        MonitoringTicket monitoringTicket = getTicketById(id);
        TicketHistory history = new TicketHistory();
        history.setMonitoringTicket(monitoringTicket);
        history.setOldStatus(monitoringTicket.getTicketStatus());
        history.setNewStatus(status);
        history.setChangedBy("system");
        monitoringTicket.getHistory().add(history);
        monitoringTicket.setTicketStatus(status);
        MonitoringTicket updatedMonitoringTicket = ticketRepository.save(monitoringTicket);
        logger.info("Ticket actualizado: ID={}, Nuevo estado={}", updatedMonitoringTicket.getId(), status);
        sendEmail(updatedMonitoringTicket, null, "Ticket Actualizado a " + status);
        return updatedMonitoringTicket;
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
        logger.info("Ticket eliminado: ID={}", id);
    }

    private void sendEmail(MonitoringTicket monitoringTicket, byte[] pdfBytes, String subject) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo("podstreamstore@gmail.com"); // Reemplazar con email real
            helper.setSubject(subject);
            helper.setText("Ticket ID: " + monitoringTicket.getId() + "\nStatus: " + monitoringTicket.getTicketStatus());
            if (pdfBytes != null) {
                helper.addAttachment("factura.pdf", new ByteArrayResource(pdfBytes));
            }
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Error al enviar correo para el ticket #{}", monitoringTicket.getId(), e);
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}
