package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.OrderStatusHistory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar el historial de cambios de estado de una orden en PodStream.
 */
@Data
public class OrderStatusHistoryDTO {

    private Long id;

    @NotNull(message = "El ID de la orden de compra es obligatorio")
    private Long purchaseOrderId;

    private Long supportTicketId;

    private OrderStatus oldStatus;

    @NotNull(message = "El nuevo estado es obligatorio")
    private OrderStatus newStatus;

    @NotBlank(message = "El usuario que realizó el cambio no puede estar vacío")
    private String changedBy;

    private LocalDateTime changeDate;

    private boolean active;

    public OrderStatusHistoryDTO() {}

    public OrderStatusHistoryDTO(OrderStatusHistory history) {
        this.id = history.getId();
        this.purchaseOrderId = history.getPurchaseOrder().getId();
        this.supportTicketId = history.getSupportTicket() != null ? history.getSupportTicket().getId() : null;
        this.oldStatus = history.getOldStatus();
        this.newStatus = history.getNewStatus();
        this.changedBy = history.getChangedBy();
        this.changeDate = history.getChangeDate();
        this.active = history.isActive();
    }
}