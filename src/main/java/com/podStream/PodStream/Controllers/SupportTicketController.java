package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.SupportTicketDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Services.SupportTicketService;
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
 * Controlador REST para gestionar tickets de soporte en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@RestController
@RequestMapping("/api/support-tickets")
@Tag(name = "Support Ticket Management", description = "APIs for managing support tickets in the PodStream e-commerce platform")
public class SupportTicketController {

    private static final Logger logger = LoggerFactory.getLogger(SupportTicketController.class);

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    @Operation(summary = "Create a new support ticket", description = "Creates a new support ticket for the authenticated client.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Support ticket created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client or purchase order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<SupportTicketDTO>> createSupportTicket(
            @Valid @RequestBody SupportTicketDTO ticketDTO,
            Authentication authentication) {
        logger.info("Creating support ticket, title: {}", ticketDTO.getTitle());
        SupportTicketDTO createdTicket = supportTicketService.createSupportTicket(ticketDTO, authentication);
        return new ResponseEntity<>(ApiResponse.success("Support ticket created", createdTicket), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a support ticket", description = "Updates an existing support ticket. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Support ticket updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Support ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupportTicketDTO>> updateSupportTicket(
            @PathVariable @Positive(message = "Ticket ID must be positive") Long id,
            @Valid @RequestBody SupportTicketDTO ticketDTO,
            Authentication authentication) {
        logger.info("Updating support ticket, ticketId: {}", id);
        SupportTicketDTO updatedTicket = supportTicketService.updateSupportTicket(id, ticketDTO, authentication);
        return ResponseEntity.ok(ApiResponse.success("Support ticket updated", updatedTicket));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update support ticket status", description = "Updates the status of a support ticket. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Support ticket status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Support ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupportTicketDTO>> updateTicketStatus(
            @PathVariable @Positive(message = "Ticket ID must be positive") Long id,
            @RequestParam OrderStatus status,
            @RequestParam @NotBlank(message = "Changed by is required") String changedBy,
            Authentication authentication) {
        logger.info("Updating support ticket status, ticketId: {}, newStatus: {}", id, status);
        SupportTicketDTO updatedTicket = supportTicketService.updateTicketStatus(id, status, changedBy, authentication);
        return ResponseEntity.ok(ApiResponse.success("Support ticket status updated", updatedTicket));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get support ticket by ID", description = "Retrieves a support ticket by its ID. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Support ticket retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Support ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupportTicketDTO>> getSupportTicket(
            @PathVariable @Positive(message = "Ticket ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Fetching support ticket, ticketId: {}", id);
        SupportTicketDTO ticket = supportTicketService.getSupportTicket(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Support ticket retrieved", ticket));
    }

    @GetMapping("/client")
    @Operation(summary = "Get support tickets by client", description = "Retrieves all active support tickets for the authenticated client.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Support tickets retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<SupportTicketDTO>>> getSupportTicketsByClient(
            Authentication authentication) {
        logger.info("Fetching support tickets for client");
        List<SupportTicketDTO> tickets = supportTicketService.getSupportTicketsByClient(authentication);
        return ResponseEntity.ok(ApiResponse.success("Support tickets retrieved", tickets));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    @Operation(summary = "Get support tickets by purchase order", description = "Retrieves all active support tickets for a purchase order. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Support tickets retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Purchase order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SupportTicketDTO>>> getSupportTicketsByPurchaseOrder(
            @PathVariable @Positive(message = "Purchase order ID must be positive") Long purchaseOrderId,
            Authentication authentication) {
        logger.info("Fetching support tickets for purchase order: {}", purchaseOrderId);
        List<SupportTicketDTO> tickets = supportTicketService.getSupportTicketsByPurchaseOrder(purchaseOrderId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Support tickets retrieved", tickets));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete support ticket", description = "Soft deletes a support ticket by setting it as inactive. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Support ticket deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Support ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSupportTicket(
            @PathVariable @Positive(message = "Ticket ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Deleting support ticket, ticketId: {}", id);
        supportTicketService.deleteSupportTicket(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Support ticket deleted", null));
    }
}