package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.OrderStatusHistoryDTO;
import com.podStream.PodStream.Models.OrderStatus;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Interfaz para gestionar el historial de cambios de estado de órdenes en PodStream.
 */
public interface OrderStatusHistoryService {
    OrderStatusHistoryDTO createHistory(Long purchaseOrderId, Long supportTicketId, OrderStatus newStatus, Authentication authentication);
    OrderStatusHistoryDTO getHistory(Long id, Authentication authentication);
    List<OrderStatusHistoryDTO> getHistoriesByPurchaseOrder(Long purchaseOrderId, Authentication authentication);
    List<OrderStatusHistoryDTO> getHistoriesByNewStatus(OrderStatus newStatus, Authentication authentication);
    List<OrderStatusHistoryDTO> getHistoriesByChangedBy(String changedBy, Authentication authentication);
    List<OrderStatusHistoryDTO> getAllHistories(Authentication authentication);
    void deleteHistory(Long id, Authentication authentication);
}
