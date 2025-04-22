package com.podStream.PodStream.Models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class MonitoringTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus = TicketStatus.OPEN;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "monitoringTicket", cascade = CascadeType.ALL)
    private List<TicketHistory> history;

    // Campos adicionales para monitoreo
    private String source; // Por ejemplo, "Prometheus", "Logstash", "Application"
    private String errorCode; // Código de error, si aplica
    private String severity; // "LOW", "MEDIUM", "HIGH"



    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TicketHistory> getHistory() {
        return history;
    }


    public void addHistory(TicketHistory ticketHistory) {
        this.history.add(ticketHistory);
        ticketHistory.setMonitoringTicket(this);
    }

    public void setHistory(List<TicketHistory> history, MonitoringTicket monitoringTicket) {
        this.history = history;
        for (TicketHistory ticketHistory : history) {
            ticketHistory.setMonitoringTicket(monitoringTicket);
        }
    }
    public void setHistory(List<TicketHistory> history) {
        this.history = history;
        for (TicketHistory ticketHistory : history) {
            ticketHistory.setMonitoringTicket(this);
        }
    }
    public void removeHistory(TicketHistory ticketHistory) {
        this.history.remove(ticketHistory);
        ticketHistory.setMonitoringTicket(null);
    }

    public void clearHistory() {
        for (TicketHistory ticketHistory : history) {
            ticketHistory.setMonitoringTicket(null);
        }
        history.clear();
    }
    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ticketStatus=" + ticketStatus +
                ", createdAt=" + createdAt +
                '}';
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}