package com.podStream.PodStream.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad que representa una orden de compra en PodStream.
 * <p>Almacena información sobre la orden, incluyendo ticket, monto, método de pago, dirección, cliente, detalles, historial de estados y tickets de soporte.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@Entity
@Table(name = "purchase_orders", indexes = {
        @Index(name = "idx_purchase_order_ticket", columnList = "ticket"),
        @Index(name = "idx_purchase_order_status", columnList = "status"),
        @Index(name = "idx_purchase_order_active", columnList = "active")
})
@Data
@EntityListeners(AuditingEntityListener.class)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El ticket no puede estar vacío")
    @Size(max = 36, message = "El ticket no puede exceder 36 caracteres")
    @Column(unique = true)
    private String ticket;

    @Positive(message = "El monto debe ser mayor a 0")
    private double amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @NotNull(message = "El método de pago no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @NotNull(message = "La dirección no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    @JsonBackReference(value = "address-purchaseOrder")
    private Address address;

    @NotNull(message = "El cliente no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonBackReference(value = "client-purchaseOrder")
    private Client client;

    @NotBlank(message = "El RUT del cliente no puede estar vacío")
    @Pattern(regexp = "^\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[0-9kK]$", message = "RUT del cliente inválido")
    private String customerRut;

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @NotEmpty(message = "La orden debe tener al menos un detalle")
    @OneToMany(mappedBy = "purchaseOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Details> details = new HashSet<>();

    @OneToMany(mappedBy = "purchaseOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "purchaseOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SupportTicket> supportTickets = new HashSet<>();

    @Column(name = "active", nullable = false)
    private boolean active = true;
}