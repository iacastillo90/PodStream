package com.podStream.PodStream.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de cambios de estado de una orden en PodStream.
 */
@Entity
@Table(name = "order_status_history")
@Data
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @NotNull(message = "La orden de compra es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_ticket_id")
    private SupportTicket supportTicket;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private OrderStatus oldStatus;

    @NotNull(message = "El nuevo estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private OrderStatus newStatus;

    @NotBlank(message = "El usuario que realizó el cambio no puede estar vacío")
    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @CreatedDate
    @Column(name = "change_date", nullable = false, updatable = false)
    private LocalDateTime changeDate;

    @Column(name = "active")
    private boolean active = true;

    public OrderStatusHistory() {
        this.changeDate = LocalDateTime.now();
    }
}