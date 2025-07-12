package com.podStream.PodStream.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un ticket de soporte en PodStream.
 * <p>Permite a los clientes reportar reclamos o inconvenientes relacionados con compras o servicios generales.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_support_ticket_title", columnList = "title"),
        @Index(name = "idx_support_ticket_status", columnList = "status"),
        @Index(name = "idx_support_ticket_active", columnList = "active")
})
@Data
@EntityListeners(AuditingEntityListener.class)
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PROCESSING;

    @NotNull(message = "El creador no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    @JsonBackReference(value = "client-supportTicket")
    private Client createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    @JsonBackReference(value = "purchaseOrder-supportTicket")
    private PurchaseOrder purchaseOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "supportTicket", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> history = new ArrayList<>();

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "jira_issue_key")
    private String jiraIssueKey;
}