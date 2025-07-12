package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.SupportTicket;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Repositorio de búsqueda para tickets de soporte en Elasticsearch en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
public interface SupportTicketElasticRepository extends ElasticsearchRepository<SupportTicket, Long> {

    /**
     * Busca tickets por título o descripción (coincidencia parcial).
     *
     * @param title       El título del ticket.
     * @param description La descripción del ticket.
     * @return Lista de tickets que coinciden.
     */
    List<SupportTicket> findByTitleContainingOrDescriptionContaining(String title, String description);

    /**
     * Busca tickets activos por estado.
     *
     * @param status El estado del ticket.
     * @return Lista de tickets en el estado especificado.
     */
    List<SupportTicket> findByStatusAndActiveTrue(OrderStatus status);
}