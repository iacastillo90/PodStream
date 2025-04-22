package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.MonitoringTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<MonitoringTicket, Long> {
}