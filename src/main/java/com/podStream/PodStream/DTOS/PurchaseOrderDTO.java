package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO para representar una orden de compra en la API REST de PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@Data
public class PurchaseOrderDTO {

    private Long id;

    @NotBlank(message = "El ticket no puede estar vacío")
    @Size(max = 36, message = "El ticket no puede exceder 36 caracteres")
    private String ticket;

    @Positive(message = "El monto debe ser mayor a 0")
    private double amount;

    @NotNull(message = "La fecha de creación no puede ser nula")
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotNull(message = "El método de pago no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Positive(message = "El ID de la dirección debe ser positivo")
    private Long addressId;

    @Positive(message = "El ID del cliente debe ser positivo")
    private Long clientId;

    @NotBlank(message = "El RUT del cliente no puede estar vacío")
    @Pattern(regexp = "^\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[0-9kK]$", message = "RUT del cliente inválido")
    private String customerRut;

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotEmpty(message = "La orden debe tener al menos un detalle")
    private Set<DetailsDTO> details = new HashSet<>();

    private List<OrderStatusHistoryDTO> statusHistory;

    private Set<SupportTicketDTO> supportTickets;

    private boolean active;

    public PurchaseOrderDTO() {}

    public PurchaseOrderDTO(PurchaseOrder purchaseOrder) {
        this.id = purchaseOrder.getId();
        this.ticket = purchaseOrder.getTicket();
        this.amount = purchaseOrder.getAmount();
        this.createdAt = purchaseOrder.getCreatedAt();
        this.updatedAt = purchaseOrder.getUpdatedAt();
        this.paymentMethod = purchaseOrder.getPaymentMethod();
        this.addressId = purchaseOrder.getAddress().getId();
        this.clientId = purchaseOrder.getClient().getId();
        this.customerRut = purchaseOrder.getCustomerRut();
        this.status = purchaseOrder.getStatus();
        this.details = purchaseOrder.getDetails().stream().map(DetailsDTO::new).collect(Collectors.toSet());
        this.statusHistory = purchaseOrder.getStatusHistory().stream().map(OrderStatusHistoryDTO::new).collect(Collectors.toList());
        this.supportTickets = purchaseOrder.getSupportTickets().stream().map(SupportTicketDTO::new).collect(Collectors.toSet());
        this.active = purchaseOrder.isActive();
    }
}