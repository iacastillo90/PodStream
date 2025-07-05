package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class PurchaseOrderDTO {

    private long id;

    /**
     * Número de ticket de la orden de compra.
     */
    private String ticket;

    /**
     * Monto total de la orden de compra.
     */
    private double amount;

    /**
     * Fecha y hora de la orden de compra.
     */
    private LocalDateTime date;

    /**
     * Método de pago utilizado en la orden de compra.
     */
    private PaymentMethod paymentMethod;

    /**
     * Dirección de envío asociada a la orden de compra.
     */
    private long address;

    /**
     * Persona que realizó la compra.
     */
    private long client;

    /**
     * RUT del cliente para fines fiscales (SII).
     */
    @NotBlank(message = "El RUT del cliente no puede estar vacío")
    @Pattern(regexp = "\\d{1,2}\\.\\d{3}\\.\\d{3}-[0-9kK]", message = "RUT del cliente inválido")
    private String customerRut;

    /**
     * Estado actual de la orden de compra.
     */
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    public PurchaseOrderDTO(PurchaseOrder purchaseOrder) {
        id = purchaseOrder.getId();
        ticket = purchaseOrder.getTicket();
        amount = purchaseOrder.getAmount();
        date = purchaseOrder.getDate();
        paymentMethod = purchaseOrder.getPaymentMethod();
        address = purchaseOrder.getAddress().getId();
        client = purchaseOrder.getClient().getId();
    }

    public long getId() {
        return id;
    }

    public String getTicket() {
        return ticket;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public long getAddress() {
        return address;
    }

    public long getClient() {
        return client;
    }

    public String getCustomerRut() {
        return customerRut;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
