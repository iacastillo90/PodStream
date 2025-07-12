package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.TicketHistory;
import com.podStream.PodStream.Models.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones CRUD de TicketHistory en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    /**
     * Encuentra historial activo por ID de ticket de monitoreo.
     *
     * @param monitoringTicketId El ID del ticket de monitoreo.
     * @return Lista de historial activo.
     */
    List<TicketHistory> findByMonitoringTicketIdAndActiveTrue(Long monitoringTicketId);

    /**
     * Encuentra historial activo por estado nuevo.
     *
     * @param newStatus El nuevo estado.
     * @return Lista de historial con el estado especificado.
     */
    List<TicketHistory> findByNewStatusAndActiveTrue(TicketStatus newStatus);
}