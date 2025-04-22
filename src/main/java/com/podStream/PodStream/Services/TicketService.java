package com.podStream.PodStream.Services;

import com.podStream.PodStream.Models.MonitoringTicket;

import java.util.List;

public interface TicketService {

    public MonitoringTicket createTicket(MonitoringTicket monitoringTicket);

    public MonitoringTicket getTicketById(Long id);

    public MonitoringTicket updateTicket(MonitoringTicket monitoringTicket);

    public void deleteTicket(Long id);

    public List<MonitoringTicket> getAllTickets();

}
