package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.MonitoringTicket;
import com.podStream.PodStream.Models.TicketHistory;
import com.podStream.PodStream.Models.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para representar un ticket de monitoreo en PodStream.
 */
@Data
public class MonitoringTicketDTO {

    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;

    @NotNull(message = "El estado del ticket es obligatorio")
    private TicketStatus ticketStatus;

    @NotBlank(message = "La fuente no puede estar vacía")
    private String source;

    private String errorCode;

    @NotBlank(message = "La severidad no puede estar vacía")
    private String severity;

    private String jiraIssueId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean active;

    private List<TicketHistoryDTO> history;

    public MonitoringTicketDTO() {}

    public MonitoringTicketDTO(MonitoringTicket ticket) {
        this.id = ticket.getId();
        this.title = ticket.getTitle();
        this.description = ticket.getDescription();
        this.ticketStatus = ticket.getTicketStatus();
        this.source = ticket.getSource();
        this.errorCode = ticket.getErrorCode();
        this.severity = ticket.getSeverity();
        this.jiraIssueId = ticket.getJiraIssueId();
        this.createdAt = ticket.getCreatedAt();
        this.updatedAt = ticket.getUpdatedAt();
        this.active = ticket.isActive();
        this.history = ticket.getHistory().stream()
                .map(TicketHistoryDTO::new)
                .collect(Collectors.toList());
    }
}