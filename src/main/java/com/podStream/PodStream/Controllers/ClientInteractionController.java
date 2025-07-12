package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.ClientInteractionDTO;
import com.podStream.PodStream.DTOS.ClientInteractionRequest;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.ClientInteractionService;
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
 * Controlador REST para gestionar interacciones de clientes con productos en PodStream.
 */
@RestController
@RequestMapping("/api/interactions")
@Tag(name = "Client Interaction Management", description = "APIs for managing client interactions with products in the PodStream e-commerce platform")
public class ClientInteractionController {

    private static final Logger logger = LoggerFactory.getLogger(ClientInteractionController.class);

    private final ClientInteractionService clientInteractionService;

    public ClientInteractionController(ClientInteractionService clientInteractionService) {
        this.clientInteractionService = clientInteractionService;
    }

    @PostMapping
    @Operation(summary = "Record a client interaction", description = "Records a client interaction with a product (e.g., view, purchase).")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Interaction recorded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientInteractionDTO>> recordInteraction(
            @Valid @RequestBody ClientInteractionRequest request,
            Authentication authentication) {
        logger.info("Recording interaction for user {} and product {}", request.getUserId(), request.getProductId());
        ClientInteractionDTO interaction = clientInteractionService.recordInteraction(request, authentication);
        return new ResponseEntity<>(ApiResponse.success("Interaction recorded", interaction), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interaction by ID", description = "Retrieves a specific client interaction by its ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interaction retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Interaction not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientInteractionDTO>> getInteraction(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Fetching interaction with id: {}", id);
        ClientInteractionDTO interaction = clientInteractionService.getInteraction(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Interaction retrieved", interaction));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get interactions by client", description = "Retrieves all interactions for a specific client.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interactions retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<ClientInteractionDTO>>> getInteractionsByClient(
            @PathVariable @Positive(message = "Client ID must be positive") Long clientId,
            Authentication authentication) {
        logger.info("Fetching interactions for client: {}", clientId);
        List<ClientInteractionDTO> interactions = clientInteractionService.getInteractionsByClient(clientId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Interactions retrieved", interactions));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an interaction", description = "Updates a specific client interaction.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interaction updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Interaction, client, or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientInteractionDTO>> updateInteraction(
            @PathVariable Long id,
            @Valid @RequestBody ClientInteractionRequest request,
            Authentication authentication) {
        logger.info("Updating interaction with id: {}", id);
        ClientInteractionDTO updatedInteraction = clientInteractionService.updateInteraction(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Interaction updated", updatedInteraction));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an interaction", description = "Soft deletes a specific client interaction.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interaction deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Interaction not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Void>> deleteInteraction(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Deleting interaction with id: {}", id);
        clientInteractionService.deleteInteraction(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Interaction deleted", null));
    }
}