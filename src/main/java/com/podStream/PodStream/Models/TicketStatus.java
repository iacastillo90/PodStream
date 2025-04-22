package com.podStream.PodStream.Models;

public enum TicketStatus {
    OPEN("Abierto"),
    IN_PROGRESS("En Progreso"),
    RESOLVED("Resuelto"),
    CLOSED("Cerrado");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}