package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.PurchaseOrderDTO;
import com.podStream.PodStream.Models.OrderStatus;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Interfaz para gestionar órdenes de compra en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
public interface PurchaseOrderService {

    /**
     * Crea una nueva orden de compra.
     *
     * @param orderDTO       El DTO de la orden.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la orden creada.
     */
    PurchaseOrderDTO createPurchaseOrder(PurchaseOrderDTO orderDTO, Authentication authentication);

    /**
     * Crea una orden de compra desde el carrito.
     *
     * @param sessionId      El ID de la sesión.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la orden creada.
     */
    PurchaseOrderDTO createPurchaseOrderFromCart(String sessionId, Authentication authentication);

    /**
     * Actualiza el estado de una orden.
     *
     * @param orderId        El ID de la orden.
     * @param newStatus      El nuevo estado.
     * @param changedBy      Quién realizó el cambio.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la orden actualizada.
     */
    PurchaseOrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus, String changedBy, Authentication authentication);

    /**
     * Obtiene una orden por su ID.
     *
     * @param orderId        El ID de la orden.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la orden.
     */
    PurchaseOrderDTO getOrder(Long orderId, Authentication authentication);

    /**
     * Obtiene una orden por su ticket.
     *
     * @param ticket         El ticket de la orden.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la orden.
     */
    PurchaseOrderDTO getOrderByTicket(String ticket, Authentication authentication);

    /**
     * Obtiene todas las órdenes activas de un cliente.
     *
     * @param authentication La autenticación del usuario.
     * @return Lista de DTOs de órdenes.
     */
    List<PurchaseOrderDTO> getOrdersByClient(Authentication authentication);

    /**
     * Elimina (soft delete) una orden.
     *
     * @param orderId        El ID de la orden.
     * @param authentication La autenticación del usuario.
     */
    void deleteOrder(Long orderId, Authentication authentication);
}