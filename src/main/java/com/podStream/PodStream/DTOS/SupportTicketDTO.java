package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.SupportTicket;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para representar un ticket de soporte en la API REST de PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@Data
public class SupportTicketDTO {

    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Positive(message = "El ID del creador debe ser positivo")
    private Long createdById;

    @Positive(message = "El ID de la orden debe ser positivo")
    private Long purchaseOrderId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<OrderStatusHistoryDTO> history = new ArrayList<>();

    private boolean active;

    private String jiraIssueKey;

    public SupportTicketDTO() {}

    public SupportTicketDTO(SupportTicket supportTicket) {
        this.id = supportTicket.getId();
        this.title = supportTicket.getTitle();
        this.description = supportTicket.getDescription();
        this.status = supportTicket.getStatus();
        this.createdById = supportTicket.getCreatedBy().getId();
        this.purchaseOrderId = supportTicket.getPurchaseOrder() != null ? supportTicket.getPurchaseOrder().getId() : null;
        this.createdAt = supportTicket.getCreatedAt();
        this.updatedAt = supportTicket.getUpdatedAt();
        this.history = supportTicket.getHistory().stream().map(OrderStatusHistoryDTO::new).collect(Collectors.toList());
        this.active = supportTicket.isActive();
        this.jiraIssueKey = supportTicket.getJiraIssueKey();
    }
}