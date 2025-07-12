package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.MonitoringTicketDTO;
import com.podStream.PodStream.DTOS.MonitoringTicketRequestDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.TicketStatus;
import com.podStream.PodStream.Services.MonitoringTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * Controlador REST para gestionar tickets de monitoreo en PodStream.
 */
@RestController
@RequestMapping("/api/monitoring-tickets")
@Tag(name = "Monitoring Ticket Management", description = "APIs for managing system monitoring tickets in the PodStream platform")
public class MonitoringTicketController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringTicketController.class);

    private final MonitoringTicketService ticketService;

    public MonitoringTicketController(MonitoringTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @Operation(summary = "Create a new monitoring ticket", description = "Creates a new monitoring ticket. Accessible to ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ticket created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MonitoringTicketDTO>> createTicket(
            @Valid @RequestBody MonitoringTicketRequestDTO request,
            Authentication authentication) {
        logger.info("Creating monitoring ticket: {}", request.getTitle());
        MonitoringTicketDTO ticket = ticketService.createTicket(request, authentication);
        return new ResponseEntity<>(ApiResponse.success("Ticket created", ticket), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get monitoring ticket by ID", description = "Retrieves a specific monitoring ticket. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<MonitoringTicketDTO>> getTicket(
            @PathVariable @Positive(message = "Ticket ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Fetching monitoring ticket with id: {}", id);
        MonitoringTicketDTO ticket = ticketService.getTicket(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved", ticket));
    }

    @GetMapping("/source/{source}")
    @Operation(summary = "Get monitoring tickets by source", description = "Retrieves all monitoring tickets for a specific source. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<List<MonitoringTicketDTO>>> getTicketsBySource(
            @PathVariable String source,
            Authentication authentication) {
        logger.info("Fetching monitoring tickets for source: {}", source);
        List<MonitoringTicketDTO> tickets = ticketService.getTicketsBySource(source, authentication);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved", tickets));
    }

    @GetMapping("/severity/{severity}")
    @Operation(summary = "Get monitoring tickets by severity", description = "Retrieves all monitoring tickets for a specific severity. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<List<MonitoringTicketDTO>>> getTicketsBySeverity(
            @PathVariable String severity,
            Authentication authentication) {
        logger.info("Fetching monitoring tickets for severity: {}", severity);
        List<MonitoringTicketDTO> tickets = ticketService.getTicketsBySeverity(severity, authentication);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved", tickets));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get monitoring tickets by status", description = "Retrieves all monitoring tickets for a specific status. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<List<MonitoringTicketDTO>>> getTicketsByStatus(
            @PathVariable TicketStatus status,
            Authentication authentication) {
        logger.info("Fetching monitoring tickets for status: {}", status);
        List<MonitoringTicketDTO> tickets = ticketService.getTicketsByStatus(status, authentication);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved", tickets));
    }

    @GetMapping
    @Operation(summary = "Get all monitoring tickets", description = "Retrieves all active monitoring tickets. Accessible to ADMIN or DEVELOPER.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<List<MonitoringTicketDTO>>> getAllTickets(
            Authentication authentication) {
        logger.info("Fetching all monitoring tickets");
        List<MonitoringTicketDTO> tickets = ticketService.getAllTickets(authentication);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved", tickets));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a monitoring ticket", description = "Updates a specific monitoring ticket. Accessible to ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MonitoringTicketDTO>> updateTicket(
            @PathVariable @Positive(message = "Ticket ID must be positive") Long id,
            @Valid @RequestBody MonitoringTicketRequestDTO request,
            Authentication authentication) {
        logger.info("Updating monitoring ticket with id: {}", id);
        MonitoringTicketDTO ticket = ticketService.updateTicket(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Ticket updated", ticket));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update monitoring ticket status", description = "Updates the status of a specific monitoring ticket. Accessible to ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MonitoringTicketDTO>> updateStatus(
            @PathVariable @Positive(message = "Ticket ID must be positive") Long id,
            @RequestParam TicketStatus status,
            Authentication authentication) {
        logger.info("Updating status of monitoring ticket id: {} to {}", id, status);
        MonitoringTicketDTO ticket = ticketService.updateStatus(id, status, authentication);
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated", ticket));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a monitoring ticket", description = "Soft deletes a specific monitoring ticket. Accessible to ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(
            @PathVariable @Positive(message = "Ticket ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Deleting monitoring ticket with id: {}", id);
        ticketService.deleteTicket(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted", null));
    }
}