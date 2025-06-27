package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
}
