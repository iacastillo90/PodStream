package com.podStream.PodStream.Models;

public enum OrderStatus {
    PENDING_PAYMENT("Pendiente de Pago"),
    PAYMENT_CONFIRMED("Pago Confirmado"),
    PROCESSING("Procesando"),
    SHIPPED("Enviado"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}