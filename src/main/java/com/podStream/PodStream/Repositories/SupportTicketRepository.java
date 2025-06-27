package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
}
