package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.ProductRatingDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.ClientRepository;
import com.podStream.PodStream.Services.ProductRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar calificaciones de productos en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Product Rating Management", description = "APIs for managing product ratings in the PodStream platform")
public class ProductRatingController {

    private static final Logger logger = LoggerFactory.getLogger(ProductRatingController.class);

    private final ProductRatingService productRatingService;

    @Autowired
    private  ClientRepository clientRepository;

    public ProductRatingController(ProductRatingService productRatingService) {
        this.productRatingService = productRatingService;
    }

    @PostMapping
    @Operation(summary = "Create a new rating", description = "Creates a new product rating. Accessible to USER.")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProductRatingDTO>> createRating(
            @Valid @RequestBody ProductRatingDTO ratingDTO,
            Authentication authentication) {
        logger.info("Creating rating for product id: {}", ratingDTO.getProductId());
        ProductRatingDTO savedRating = productRatingService.createRating(ratingDTO, authentication);
        return new ResponseEntity<>(ApiResponse.success("Rating created", savedRating), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a rating by ID", description = "Retrieves a specific rating.")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<ProductRatingDTO>> getRating(
            @PathVariable @Positive(message = "Rating ID must be positive") Long id) {
        logger.info("Fetching rating with id: {}", id);
        ProductRatingDTO rating = productRatingService.getRating(id);
        return ResponseEntity.ok(ApiResponse.success("Rating retrieved", rating));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get ratings by product", description = "Retrieves all active ratings for a product.")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<ProductRatingDTO>>> getRatingsByProduct(
            @PathVariable @Positive(message = "Product ID must be positive") Long productId) {
        logger.info("Fetching ratings for product id: {}", productId);
        List<ProductRatingDTO> ratings = productRatingService.getRatingsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Ratings retrieved", ratings));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get ratings by client", description = "Retrieves all active ratings for a client. Accessible to USER.")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<ProductRatingDTO>>> getRatingsByClient(
            @PathVariable @Positive(message = "Client ID must be positive") Long clientId,
            Authentication authentication) {
        logger.info("Fetching ratings for client id: {}", clientId);
        // Validar que el cliente autenticado es el dueño
        String username = authentication.getName();
        if (!clientRepository.findById(clientId).map(Client::getUsername).orElse("").equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only the client can access their ratings"));
        }
        List<ProductRatingDTO> ratings = productRatingService.getRatingsByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success("Ratings retrieved", ratings));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a rating", description = "Updates a specific rating. Accessible to USER.")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProductRatingDTO>> updateRating(
            @PathVariable @Positive(message = "Rating ID must be positive") Long id,
            @Valid @RequestBody ProductRatingDTO ratingDTO,
            Authentication authentication) {
        logger.info("Updating rating with id: {}", id);
        ProductRatingDTO updatedRating = productRatingService.updateRating(id, ratingDTO, authentication);
        return ResponseEntity.ok(ApiResponse.success("Rating updated", updatedRating));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a rating", description = "Soft deletes a specific rating. Accessible to USER.")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteRating(
            @PathVariable @Positive(message = "Rating ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Deleting rating with id: {}", id);
        productRatingService.deleteRating(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Rating deleted", null));
    }
}