package com.podStream.PodStream.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de cambios de estado de un ticket de monitoreo en PodStream.
 * <p>Registra transiciones de estado, quién las realizó y cuándo ocurrieron.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@Entity
@Table(name = "ticket_history", indexes = {
        @Index(name = "idx_ticket_history_monitoring_ticket_id", columnList = "monitoring_ticket_id"),
        @Index(name = "idx_ticket_history_new_status", columnList = "new_status"),
        @Index(name = "idx_ticket_history_active", columnList = "active")
})
@Data
@EntityListeners(AuditingEntityListener.class)
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ticket de monitoreo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitoring_ticket_id", nullable = false)
    @JsonBackReference(value = "monitoringTicket-ticketHistory")
    private MonitoringTicket monitoringTicket;

    @NotNull(message = "El estado anterior es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private TicketStatus oldStatus;

    @NotNull(message = "El nuevo estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private TicketStatus newStatus;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @NotNull(message = "El ID del usuario que realizó el cambio es obligatorio")
    @Column(name = "changed_by_id", nullable = false)
    private Long changedById;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}