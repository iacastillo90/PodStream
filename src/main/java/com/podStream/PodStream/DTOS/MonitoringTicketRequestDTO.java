package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para recibir solicitudes de creación o actualización de tickets de monitoreo.
 */
@Data
public class MonitoringTicketRequestDTO {

    @NotBlank(message = "El título no puede estar vacío")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;

    @NotNull(message = "El estado del ticket es obligatorio")
    private TicketStatus ticketStatus;

    @NotBlank(message = "La fuente no puede estar vacía")
    private String source;

    private String errorCode;

    @NotBlank(message = "La severidad no puede estar vacía")
    private String severity;
}