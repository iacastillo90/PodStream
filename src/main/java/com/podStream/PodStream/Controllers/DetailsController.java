package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.DetailsDTO;
import com.podStream.PodStream.DTOS.DetailsRequestDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.DetailsService;
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
 * Controlador REST para gestionar los detalles de órdenes de compra en PodStream.
 */
@RestController
@RequestMapping("/api/details")
@Tag(name = "Details Management", description = "APIs for managing order details in the PodStream e-commerce platform")
public class DetailsController {

    private static final Logger logger = LoggerFactory.getLogger(DetailsController.class);

    private final DetailsService detailsService;

    public DetailsController(DetailsService detailsService) {
        this.detailsService = detailsService;
    }

    @PostMapping
    @Operation(summary = "Create a new detail", description = "Creates a new detail for a purchase order. Accessible to authenticated clients.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Detail created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Purchase order or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<DetailsDTO>> createDetails(
            @Valid @RequestBody DetailsRequestDTO request,
            Authentication authentication) {
        logger.info("Creating details for purchase order {} and product {}", request.getPurchaseOrderId(), request.getProductId());
        DetailsDTO details = detailsService.createDetails(request, authentication);
        return new ResponseEntity<>(ApiResponse.success("Detail created", details), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detail by ID", description = "Retrieves a specific detail by its ID. Accessible to the order's owner or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detail retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Detail not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @detailsServiceImpl.existsByIdAndClientId(#id, authentication.principal.id))")
    public ResponseEntity<ApiResponse<DetailsDTO>> getDetails(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Fetching details with id: {}", id);
        DetailsDTO details = detailsService.getDetails(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Detail retrieved", details));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    @Operation(summary = "Get details by purchase order", description = "Retrieves all details for a specific purchase order. Accessible to the order's owner or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Purchase order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @detailsServiceImpl.existsByPurchaseOrderIdAndClientId(#purchaseOrderId, authentication.principal.id))")
    public ResponseEntity<ApiResponse<List<DetailsDTO>>> getDetailsByPurchaseOrder(
            @PathVariable @Positive(message = "Purchase order ID must be positive") Long purchaseOrderId,
            Authentication authentication) {
        logger.info("Fetching details for purchase order: {}", purchaseOrderId);
        List<DetailsDTO> details = detailsService.getDetailsByPurchaseOrder(purchaseOrderId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Details retrieved", details));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get details by product", description = "Retrieves all details for a specific product. Accessible to the order's owner or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<DetailsDTO>>> getDetailsByProduct(
            @PathVariable @Positive(message = "Product ID must be positive") Long productId,
            Authentication authentication) {
        logger.info("Fetching details for product: {}", productId);
        List<DetailsDTO> details = detailsService.getDetailsByProduct(productId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Details retrieved", details));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a detail", description = "Updates a specific detail. Accessible only to the order's owner.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detail updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Detail, purchase order, or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') and @detailsServiceImpl.existsByIdAndClientId(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<DetailsDTO>> updateDetails(
            @PathVariable Long id,
            @Valid @RequestBody DetailsRequestDTO request,
            Authentication authentication) {
        logger.info("Updating details with id: {}", id);
        DetailsDTO updatedDetails = detailsService.updateDetails(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Detail updated", updatedDetails));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a detail", description = "Soft deletes a specific detail. Accessible to ADMIN or the order's owner.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detail deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Detail not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @detailsServiceImpl.existsByIdAndClientId(#id, authentication.principal.id))")
    public ResponseEntity<ApiResponse<Void>> deleteDetails(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Deleting details with id: {}", id);
        detailsService.deleteDetails(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Detail deleted", null));
    }
}