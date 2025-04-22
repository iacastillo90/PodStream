package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.Models.SupportTicket;
import com.podStream.PodStream.Services.SupportTicketServiceImplement;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support-tickets")
public class SupportTicketController {

    @Autowired
    private SupportTicketServiceImplement supportTicketService;

    @PostMapping("/create")
    @Operation(summary = "Create a new support ticket")
    public ResponseEntity<SupportTicket> createSupportTicket(@Valid @RequestBody SupportTicket ticket) {
        return ResponseEntity.ok(supportTicketService.createSupportTicket(ticket));
    }

}