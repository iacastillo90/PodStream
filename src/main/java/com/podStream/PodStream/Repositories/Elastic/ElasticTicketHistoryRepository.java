package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.TicketHistory;
import com.podStream.PodStream.Models.TicketStatus;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Repositorio de búsqueda para historial de tickets en Elasticsearch en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
public interface ElasticTicketHistoryRepository extends ElasticsearchRepository<TicketHistory, Long> {

    /**
     * Busca historial activo por ID del usuario que realizó el cambio.
     *
     * @param changedById El ID del usuario.
     * @return Lista de historial activo.
     */
    List<TicketHistory> findByChangedByIdAndActiveTrue(Long changedById);

    /**
     * Busca historial activo por estado nuevo.
     *
     * @param newStatus El nuevo estado.
     * @return Lista de historial con el estado especificado.
     */
    List<TicketHistory> findByNewStatusAndActiveTrue(TicketStatus newStatus);
}