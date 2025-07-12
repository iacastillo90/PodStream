package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de PurchaseOrder en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /**
     * Encuentra órdenes activas por ID de cliente.
     *
     * @param clientId El ID del cliente.
     * @return Lista de órdenes activas.
     */
    List<PurchaseOrder> findByClientIdAndActiveTrue(Long clientId);

    /**
     * Encuentra una orden activa por su ticket.
     *
     * @param ticket El ticket de la orden.
     * @return Optional con la orden, si existe.
     */
    Optional<PurchaseOrder> findByTicketAndActiveTrue(String ticket);

    /**
     * Encuentra órdenes activas por estado.
     *
     * @param status El estado de la orden.
     * @return Lista de órdenes en el estado especificado.
     */
    List<PurchaseOrder> findByStatusAndActiveTrue(OrderStatus status);

    /**
     * Encuentra órdenes activas creadas después de una fecha.
     *
     * @param date La fecha límite.
     * @return Lista de órdenes creadas después de la fecha.
     */
    List<PurchaseOrder> findByCreatedAtAfterAndActiveTrue(LocalDateTime date);
}