package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.SupportTicketDTO;
import com.podStream.PodStream.Models.OrderStatus;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Interfaz para gestionar tickets de soporte en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
public interface SupportTicketService {

    /**
     * Crea un nuevo ticket de soporte.
     *
     * @param ticketDTO      El DTO del ticket.
     * @param authentication La autenticación del usuario.
     * @return El DTO del ticket creado.
     */
    SupportTicketDTO createSupportTicket(SupportTicketDTO ticketDTO, Authentication authentication);

    /**
     * Actualiza un ticket de soporte existente.
     *
     * @param ticketId       El ID del ticket.
     * @param ticketDTO      El DTO con los datos actualizados.
     * @param authentication La autenticación del usuario.
     * @return El DTO del ticket actualizado.
     */
    SupportTicketDTO updateSupportTicket(Long ticketId, SupportTicketDTO ticketDTO, Authentication authentication);

    /**
     * Actualiza el estado de un ticket de soporte.
     *
     * @param ticketId       El ID del ticket.
     * @param newStatus      El nuevo estado.
     * @param changedBy      Quién realizó el cambio.
     * @param authentication La autenticación del usuario.
     * @return El DTO del ticket actualizado.
     */
    SupportTicketDTO updateTicketStatus(Long ticketId, OrderStatus newStatus, String changedBy, Authentication authentication);

    /**
     * Obtiene un ticket de soporte por su ID.
     *
     * @param ticketId       El ID del ticket.
     * @param authentication La autenticación del usuario.
     * @return El DTO del ticket.
     */
    SupportTicketDTO getSupportTicket(Long ticketId, Authentication authentication);

    /**
     * Obtiene todos los tickets de soporte activos de un cliente.
     *
     * @param authentication La autenticación del usuario.
     * @return Lista de DTOs de tickets.
     */
    List<SupportTicketDTO> getSupportTicketsByClient(Authentication authentication);

    /**
     * Obtiene todos los tickets de soporte activos de una orden de compra.
     *
     * @param purchaseOrderId El ID de la orden de compra.
     * @param authentication  La autenticación del usuario.
     * @return Lista de DTOs de tickets.
     */
    List<SupportTicketDTO> getSupportTicketsByPurchaseOrder(Long purchaseOrderId, Authentication authentication);

    /**
     * Elimina (soft delete) un ticket de soporte.
     *
     * @param ticketId       El ID del ticket.
     * @param authentication La autenticación del usuario.
     */
    void deleteSupportTicket(Long ticketId, Authentication authentication);
}