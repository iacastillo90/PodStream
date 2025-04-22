package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.Models.MonitoringTicket;
import com.podStream.PodStream.Models.TicketStatus;
import com.podStream.PodStream.Services.Implement.TicketServiceImplement;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketServiceImplement ticketServiceImplement;

    @PostMapping("/create")
    @Operation(summary = "Create a new ticket")
    public ResponseEntity<MonitoringTicket> createTicket(@Valid @RequestBody MonitoringTicket monitoringTicket) {
        return ResponseEntity.ok(ticketServiceImplement.createTicket(monitoringTicket));
    }

    @GetMapping
    public ResponseEntity<List<MonitoringTicket>> getAllTickets() {
        return ResponseEntity.ok(ticketServiceImplement.getAllTickets());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<MonitoringTicket> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketServiceImplement.getTicketById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update ticket status")
    public ResponseEntity<MonitoringTicket> updateStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        return ResponseEntity.ok(ticketServiceImplement.updateStatus(id, status));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketServiceImplement.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}