package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.TicketHistoryDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.TicketHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
 * Controlador REST para gestionar el historial de tickets de monitoreo en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0
 * @since 2025-07-10
 */
@RestController
@RequestMapping("/api/ticket-history")
@Tag(name = "Ticket History Management", description = "APIs for managing ticket history in the PodStream platform")
public class TicketHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(TicketHistoryController.class);

    private final TicketHistoryService ticketHistoryService;

    public TicketHistoryController(TicketHistoryService historyService) {
        this.ticketHistoryService = historyService;
    }

    @PostMapping
    @Operation(summary = "Create a new ticket history entry", description = "Creates a new history entry for a monitoring ticket. Accessible to ADMIN only.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "History entry created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Monitoring ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TicketHistoryDTO>> createTicketHistory(
            @Valid @NotNull @RequestBody TicketHistoryDTO historyDTO,
            Authentication authentication) {
        logger.info("Creating ticket history for ticket: {}", historyDTO.getMonitoringTicketId());
        TicketHistoryDTO createdHistory = ticketHistoryService.createTicketHistory(historyDTO, authentication);
        return new ResponseEntity<>(ApiResponse.success("Ticket history created", createdHistory), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket history by ID", description = "Retrieves a ticket history entry by its ID. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History entry retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "History entry not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_CLIENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TicketHistoryDTO>> getTicketHistory(
            @PathVariable @Positive(message = "History ID must be positive") Long id,
            @NotNull Authentication authentication) {
        logger.info("Fetching ticket history, historyId: {}", id);
        TicketHistoryDTO history = ticketHistoryService.getTicketHistory(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Ticket history retrieved successfully", history));
    }

    @GetMapping("/monitoring-ticket/{monitoringTicketId}")
    @Operation(summary = "Get ticket history by monitoring ticket ID", description = "Retrieves all active ticket history for a monitoring ticket. Accessible to CLIENT or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Monitoring ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_CLIENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<TicketHistoryDTO>>> getTicketHistoryByMonitoringTicket(
            @PathVariable @Positive(message = "Monitoring ticket ID must be positive") Long monitoringTicketId,
            @NotNull Authentication authentication) {
        logger.info("Fetching ticket history for monitoring ticket: {}", monitoringTicketId);
        List<TicketHistoryDTO> historyList = ticketHistoryService.getTicketHistoryByMonitoringTicket(monitoringTicketId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Ticket history retrieved successfully", historyList));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket history", description = "Soft deletes a ticket history ticket by setting it as inactive. Accessible to ADMIN only.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket history deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "History entry not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTicketHistory(
            @PathVariable @Positive(message = "History ID must be positive") Long id,
            @NotNull Authentication authentication) {
        logger.info("Deleting ticket history, historyId: {}", id);
        ticketHistoryService.deleteTicketHistory(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Ticket history deleted successfully", null));
    }
}
