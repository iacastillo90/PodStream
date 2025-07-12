package com.podStream.PodStream.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un ticket de monitoreo de fallos del sistema en PodStream.
 */
@Entity
@Table(name = "monitoring_tickets")
@Data
public class MonitoringTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "El estado del ticket es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus ticketStatus = TicketStatus.OPEN;

    @NotBlank(message = "La fuente no puede estar vacía")
    @Column(nullable = false)
    private String source;

    @Column(name = "error_code")
    private String errorCode;

    @NotBlank(message = "La severidad no puede estar vacía")
    @Column(nullable = false)
    private String severity;

    @Column(name = "jira_issue_id")
    private String jiraIssueId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "active")
    private boolean active = true;

    @OneToMany(mappedBy = "monitoringTicket", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketHistory> history = new ArrayList<>();

    public MonitoringTicket() {
        this.createdAt = LocalDateTime.now();
    }
}