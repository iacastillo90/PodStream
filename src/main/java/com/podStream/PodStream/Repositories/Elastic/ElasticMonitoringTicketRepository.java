package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.MonitoringTicket;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Repositorio Elasticsearch para búsquedas avanzadas de MonitoringTicket.
 */
public interface ElasticMonitoringTicketRepository extends ElasticsearchRepository<MonitoringTicket, Long> {
    List<MonitoringTicket> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
    List<MonitoringTicket> findByErrorCode(String errorCode);
}
