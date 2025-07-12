package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.TicketHistory;
import com.podStream.PodStream.Models.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar el historial de cambios de un ticket de monitoreo en la API REST de PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@Data
public class TicketHistoryDTO {

    private Long id;

    @NotNull(message = "El ID del ticket de monitoreo es obligatorio")
    @Positive(message = "El ID del ticket de monitoreo debe ser positivo")
    private Long monitoringTicketId;

    @NotNull(message = "El estado anterior es obligatorio")
    private TicketStatus oldStatus;

    @NotNull(message = "El nuevo estado es obligatorio")
    private TicketStatus newStatus;

    private LocalDateTime changedAt;

    @NotNull(message = "El ID del usuario que realizó el cambio es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Long changedById;

    private boolean active;

    public TicketHistoryDTO() {}

    public TicketHistoryDTO(TicketHistory history) {
        this.id = history.getId();
        this.monitoringTicketId = history.getMonitoringTicket().getId();
        this.oldStatus = history.getOldStatus();
        this.newStatus = history.getNewStatus();
        this.changedAt = history.getChangedAt();
        this.changedById = history.getChangedById();
        this.active = history.isActive();
    }
}