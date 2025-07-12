package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.TicketHistoryDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Interfaz para gestionar el historial de tickets de monitoreo en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
public interface TicketHistoryService {

    /**
     * Crea un nuevo registro de historial para un ticket de monitoreo.
     *
     * @param ticketHistoryDTO El DTO del historial.
     * @param authentication   La autenticación del usuario.
     * @return El DTO del historial creado.
     */
    TicketHistoryDTO createTicketHistory(TicketHistoryDTO ticketHistoryDTO, Authentication authentication);

    /**
     * Obtiene un registro de historial por su ID.
     *
     * @param historyId      El ID del historial.
     * @param authentication La autenticación del usuario.
     * @return El DTO del historial.
     */
    TicketHistoryDTO getTicketHistory(Long historyId, Authentication authentication);

    /**
     * Obtiene todo el historial activo de un ticket de monitoreo.
     *
     * @param monitoringTicketId El ID del ticket de monitoreo.
     * @param authentication     La autenticación del usuario.
     * @return Lista de DTOs de historial.
     */
    List<TicketHistoryDTO> getTicketHistoryByMonitoringTicket(Long monitoringTicketId, Authentication authentication);

    /**
     * Elimina (soft delete) un registro de historial.
     *
     * @param historyId      El ID del historial.
     * @param authentication La autenticación del usuario.
     */
    void deleteTicketHistory(Long historyId, Authentication authentication);
}