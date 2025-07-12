package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.PurchaseOrder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de búsqueda para órdenes de compra en Elasticsearch en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
public interface ElasticPurchaseOrderRepository extends ElasticsearchRepository<PurchaseOrder, Long> {

    /**
     * Busca órdenes por ticket o RUT del cliente (coincidencia parcial).
     *
     * @param ticket      El ticket de la orden.
     * @param customerRut El RUT del cliente.
     * @return Lista de órdenes que coinciden.
     */
    List<PurchaseOrder> findByTicketContainingOrCustomerRutContaining(String ticket, String customerRut);

    /**
     * Busca órdenes activas por estado.
     *
     * @param status El estado de la orden.
     * @return Lista de órdenes en el estado especificado.
     */
    List<PurchaseOrder> findByStatusAndActiveTrue(OrderStatus status);

    /**
     * Busca órdenes activas creadas después de una fecha.
     *
     * @param date La fecha límite.
     * @return Lista de órdenes creadas después de la fecha.
     */
    List<PurchaseOrder> findByCreatedAtAfterAndActiveTrue(LocalDateTime date);
}