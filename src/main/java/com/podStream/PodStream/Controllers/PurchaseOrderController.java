package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.PurchaseOrderDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Services.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
 * Controlador REST para gestionar órdenes de compra en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for managing purchase orders in the PodStream e-commerce platform")
public class PurchaseOrderController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    @Operation(summary = "Create a new purchase order", description = "Creates a new purchase order for the authenticated client.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client or address not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock or duplicate ticket"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> createOrder(
            @Valid @RequestBody PurchaseOrderDTO orderDTO,
            Authentication authentication) {
        logger.info("Creating purchase order, ticket: {}", orderDTO.getTicket());
        PurchaseOrderDTO createdOrder = purchaseOrderService.createPurchaseOrder(orderDTO, authentication);
        return new ResponseEntity<>(ApiResponse.success("Order created", createdOrder), HttpStatus.CREATED);
    }

    @PostMapping("/create-from-cart")
    @Operation(summary = "Create order from cart", description = "Creates a purchase order from the authenticated client's cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid session ID or empty cart"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client or address not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> createOrderFromCart(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Authentication authentication) {
        logger.info("Creating order from cart, sessionId: {}", sessionId);
        PurchaseOrderDTO createdOrder = purchaseOrderService.createPurchaseOrderFromCart(sessionId, authentication);
        return new ResponseEntity<>(ApiResponse.success("Order created from cart", createdOrder), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates the status of a purchase order. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> updateStatus(
            @PathVariable @Positive(message = "Order ID must be positive") Long id,
            @RequestParam OrderStatus status,
            @RequestParam @NotBlank(message = "Changed by is required") String changedBy,
            Authentication authentication) {
        logger.info("Updating order status, orderId: {}, newStatus: {}", id, status);
        PurchaseOrderDTO updatedOrder = purchaseOrderService.updateOrderStatus(id, status, changedBy, authentication);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", updatedOrder));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves a purchase order by its ID. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> getOrder(
            @PathVariable @Positive(message = "Order ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Fetching order, orderId: {}", id);
        PurchaseOrderDTO order = purchaseOrderService.getOrder(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    @GetMapping("/ticket/{ticket}")
    @Operation(summary = "Get order by ticket", description = "Retrieves a purchase order by its ticket. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> getOrderByTicket(
            @PathVariable @NotBlank(message = "Ticket is required") String ticket,
            Authentication authentication) {
        logger.info("Fetching order by ticket: {}", ticket);
        PurchaseOrderDTO order = purchaseOrderService.getOrderByTicket(ticket, authentication);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    @GetMapping
    @Operation(summary = "Get orders by client", description = "Retrieves all active purchase orders for the authenticated client.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getOrdersByClient(
            Authentication authentication) {
        logger.info("Fetching orders for client");
        List<PurchaseOrderDTO> orders = purchaseOrderService.getOrdersByClient(authentication);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order", description = "Soft deletes a purchase order by setting it as inactive. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @PathVariable @Positive(message = "Order ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Deleting order, orderId: {}", id);
        purchaseOrderService.deleteOrder(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Order deleted", null));
    }
}