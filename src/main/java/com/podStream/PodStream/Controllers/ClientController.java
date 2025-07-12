package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.ChangePasswordDTO;
import com.podStream.PodStream.DTOS.NewClientDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@Tag(name = "Client Management", description = "APIs for managing clients in the PodStream e-commerce platform")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientService clientService;

    @GetMapping
    @Operation(summary = "List all clients", description = "Retrieves a list of all clients. Accessible only to ADMIN users.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of clients retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied for non-admin users"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<NewClientDTO>>> getAllClients() {
        logger.info("Fetching all clients");
        return ResponseEntity.ok(ApiResponse.success("Clients retrieved", clientService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a client by ID", description = "Retrieves a client's details by their ID. Accessible to ADMIN or the client themselves.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and #id == authentication.principal.id)")
    public ResponseEntity<ApiResponse<NewClientDTO>> getClientById(@PathVariable Long id) {
        logger.info("Fetching client with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Client retrieved", clientService.findById(id)));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get a client by email", description = "Retrieves a client's details by their email. Accessible only to ADMIN users.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NewClientDTO>> getClientByEmail(@PathVariable String email) {
        logger.info("Fetching client with email: {}", email);
        return ResponseEntity.ok(ApiResponse.success("Client retrieved", clientService.findByEmail(email)));
    }

    @PostMapping
    @Operation(summary = "Create a new client", description = "Creates a new client in the system. Accessible to all users (registration endpoint).")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or username already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<NewClientDTO>> createNewClient(@Valid @RequestBody NewClientDTO client) {
        logger.info("Creating client with email: {}", client.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Client created", clientService.createNewClient(client)));
    }

    @PatchMapping("/{id}/unsubscribe")
    @Operation(summary = "Unsubscribe a client", description = "Deactivates a client's account. Accessible only to the client themselves.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client unsubscribed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Client already unsubscribed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') and #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<NewClientDTO>> unSuscribeClient(@PathVariable Long id) {
        logger.info("Unsubscribing client with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Client unsubscribed", clientService.unSuscribeClient(id)));
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate a client", description = "Reactivates a client's account. Accessible only to the client themselves.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client reactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Client already reactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') and #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<NewClientDTO>> reactivateClient(@PathVariable Long id) {
        logger.info("Reactivating client with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Client reactivated", clientService.reactivateClient(id)));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Change client password", description = "Changes a client's password after validating the old password. Accessible only to the client themselves.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid old password"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') and #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<NewClientDTO>> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordDTO passwordDTO) {
        logger.info("Changing password for client with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Password changed", clientService.changePassword(id, passwordDTO.getOldPassword(), passwordDTO.getNewPassword())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a client", description = "Updates a client's details. Accessible only to the client themselves.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or username already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') and #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<NewClientDTO>> updateClient(@PathVariable Long id, @Valid @RequestBody NewClientDTO client) {
        logger.info("Updating client with id: {}", id);
        client.setId(id);
        return ResponseEntity.ok(ApiResponse.success("Client updated", clientService.updateClient(id, client)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client", description = "Permanently deletes a client. Accessible only to ADMIN users.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Client deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        logger.info("Deleting client with id: {}", id);
        clientService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Client deleted", null));
    }
}