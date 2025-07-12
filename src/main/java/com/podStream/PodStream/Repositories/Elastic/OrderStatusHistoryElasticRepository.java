package com.podStream.PodStream.Repositories.Elastic;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.OrderStatusHistory;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Repositorio Elasticsearch para búsquedas avanzadas de OrderStatusHistory.
 */
public interface OrderStatusHistoryElasticRepository extends ElasticsearchRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByChangedByContainingIgnoreCase(String changedBy);
    List<OrderStatusHistory> findByNewStatus(OrderStatus newStatus);
}