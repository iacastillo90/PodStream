package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de SupportTicket en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    /**
     * Encuentra tickets de soporte activos por ID de cliente.
     *
     * @param clientId El ID del cliente.
     * @return Lista de tickets activos.
     */
    List<SupportTicket> findByCreatedByIdAndActiveTrue(Long clientId);

    /**
     * Encuentra tickets de soporte activos por ID de orden de compra.
     *
     * @param purchaseOrderId El ID de la orden de compra.
     * @return Lista de tickets activos.
     */
    List<SupportTicket> findByPurchaseOrderIdAndActiveTrue(Long purchaseOrderId);

    /**
     * Encuentra tickets de soporte activos por estado.
     *
     * @param status El estado del ticket.
     * @return Lista de tickets en el estado especificado.
     */
    List<SupportTicket> findByStatusAndActiveTrue(OrderStatus status);

    /**
     * Encuentra un ticket de soporte activo por su clave de Jira.
     *
     * @param jiraIssueKey La clave del issue en Jira.
     * @return Optional con el ticket, si existe.
     */
    Optional<SupportTicket> findByJiraIssueKeyAndActiveTrue(String jiraIssueKey);
}