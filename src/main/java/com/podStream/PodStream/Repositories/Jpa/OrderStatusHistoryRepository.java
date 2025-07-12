package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones CRUD de OrderStatusHistory.
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByPurchaseOrderIdAndActiveTrue(Long purchaseOrderId);
    List<OrderStatusHistory> findByNewStatusAndActiveTrue(OrderStatus newStatus);
    List<OrderStatusHistory> findByChangedByAndActiveTrue(String changedBy);
    List<OrderStatusHistory> findByActiveTrue();
}
