package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.MonitoringTicketDTO;
import com.podStream.PodStream.DTOS.MonitoringTicketRequestDTO;
import com.podStream.PodStream.Models.MonitoringTicket;
import com.podStream.PodStream.Models.TicketStatus;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Interfaz para gestionar tickets de monitoreo en PodStream.
 */
public interface MonitoringTicketService {
    MonitoringTicketDTO createTicket(MonitoringTicketRequestDTO request, Authentication authentication);
    MonitoringTicketDTO getTicket(Long id, Authentication authentication);
    List<MonitoringTicketDTO> getTicketsBySource(String source, Authentication authentication);
    List<MonitoringTicketDTO> getTicketsBySeverity(String severity, Authentication authentication);
    List<MonitoringTicketDTO> getTicketsByStatus(TicketStatus status, Authentication authentication);
    List<MonitoringTicketDTO> getAllTickets(Authentication authentication);
    MonitoringTicketDTO updateTicket(Long id, MonitoringTicketRequestDTO request, Authentication authentication);
    MonitoringTicketDTO updateStatus(Long id, TicketStatus status, Authentication authentication);
    void deleteTicket(Long id, Authentication authentication);

}
