package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.MonitoringTicket;
import com.podStream.PodStream.Models.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones CRUD de MonitoringTicket.
 */
@Repository
public interface MonitoringTicketRepository extends JpaRepository<MonitoringTicket, Long> {
    List<MonitoringTicket> findBySourceAndActiveTrue(String source);
    List<MonitoringTicket> findBySeverityAndActiveTrue(String severity);
    List<MonitoringTicket> findByTicketStatusAndActiveTrue(TicketStatus status);
    List<MonitoringTicket> findByActiveTrue();
}
