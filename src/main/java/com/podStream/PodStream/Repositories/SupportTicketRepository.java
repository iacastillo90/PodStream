package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
}
