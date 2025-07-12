package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.OrderStatusHistoryDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Services.OrderStatusHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar el historial de cambios de estado de órdenes en PodStream.
 */
@RestController
@RequestMapping("/api/order-status-history")
@Tag(name = "Order Status History Management", description = "APIs for managing order status history in the PodStream platform")
public class OrderStatusHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusHistoryController.class);

    private final OrderStatusHistoryService historyService;

    public OrderStatusHistoryController(OrderStatusHistoryService historyService) {
        this.historyService = historyService;
    }

    @PostMapping
    @Operation(summary = "Create a new order status history", description = "Creates a new order status history entry. Accessible to ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "History entry created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Purchase order or support ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderStatusHistoryDTO>> createHistory(
            @RequestParam @Positive(message = "Purchase order ID must be positive") Long purchaseOrderId,
            @RequestParam(required = false) Long supportTicketId,
            @RequestParam OrderStatus newStatus,
            Authentication authentication) {
        logger.info("Creating order status history for purchase order: {}", purchaseOrderId);
        OrderStatusHistoryDTO history = historyService.createHistory(purchaseOrderId, supportTicketId, newStatus, authentication);
        return new ResponseEntity<>(ApiResponse.success("History entry created", history), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order status history by ID", description = "Retrieves a specific order status history entry. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History entry retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "History entry not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<OrderStatusHistoryDTO>> getHistory(
            @PathVariable @Positive(message = "History ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Fetching order status history with id: {}", id);
        OrderStatusHistoryDTO history = historyService.getHistory(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("History entry retrieved", history));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    @Operation(summary = "Get order status histories by purchase order", description = "Retrieves all order status history entries for a specific purchase order. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History entries retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryDTO>>> getHistoriesByPurchaseOrder(
            @PathVariable @Positive(message = "Purchase order ID must be positive") Long purchaseOrderId,
            Authentication authentication) {
        logger.info("Fetching order status histories for purchase order: {}", purchaseOrderId);
        List<OrderStatusHistoryDTO> histories = historyService.getHistoriesByPurchaseOrder(purchaseOrderId, authentication);
        return ResponseEntity.ok(ApiResponse.success("History entries retrieved", histories));
    }

    @GetMapping("/status/{newStatus}")
    @Operation(summary = "Get order status histories by new status", description = "Retrieves all order status history entries for a specific status. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History entries retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryDTO>>> getHistoriesByNewStatus(
            @PathVariable OrderStatus newStatus,
            Authentication authentication) {
        logger.info("Fetching order status histories for new status: {}", newStatus);
        List<OrderStatusHistoryDTO> histories = historyService.getHistoriesByNewStatus(newStatus, authentication);
        return ResponseEntity.ok(ApiResponse.success("History entries retrieved", histories));
    }

    @GetMapping("/changed-by/{changedBy}")
    @Operation(summary = "Get order status histories by changed by", description = "Retrieves all order status history entries for a specific user. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History entries retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryDTO>>> getHistoriesByChangedBy(
            @PathVariable String changedBy,
            Authentication authentication) {
        logger.info("Fetching order status histories for changedBy: {}", changedBy);
        List<OrderStatusHistoryDTO> histories = historyService.getHistoriesByChangedBy(changedBy, authentication);
        return ResponseEntity.ok(ApiResponse.success("History entries retrieved", histories));
    }

    @GetMapping
    @Operation(summary = "Get all order status histories", description = "Retrieves all active order status history entries. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History entries retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryDTO>>> getAllHistories(
            Authentication authentication) {
        logger.info("Fetching all order status histories");
        List<OrderStatusHistoryDTO> histories = historyService.getAllHistories(authentication);
        return ResponseEntity.ok(ApiResponse.success("History entries retrieved", histories));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order status history", description = "Soft deletes a specific order status history entry. Accessible to ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History entry deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "History entry not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @PathVariable @Positive(message = "History ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Deleting order status history with id: {}", id);
        historyService.deleteHistory(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("History entry deleted", null));
    }
}