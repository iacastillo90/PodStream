package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.PromotionDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.PromotionService;
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
 * Controlador REST para gestionar promociones en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-09
 */
@RestController
@RequestMapping("/api/promotions")
@Tag(name = "Promotion Management", description = "APIs for managing promotions in the PodStream platform")
public class PromotionController {

    private static final Logger logger = LoggerFactory.getLogger(PromotionController.class);

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    @Operation(summary = "Create a new promotion", description = "Creates a new promotion. Accessible to ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionDTO>> createPromotion(
            @Valid @RequestBody PromotionDTO promotionDTO,
            Authentication authentication) {
        logger.info("Creating promotion with code: {}", promotionDTO.getCode());
        PromotionDTO savedPromotion = promotionService.createPromotion(promotionDTO, authentication);
        return new ResponseEntity<>(ApiResponse.success("Promotion created", savedPromotion), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a promotion by ID", description = "Retrieves a specific promotion.")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<PromotionDTO>> getPromotion(
            @PathVariable @Positive(message = "Promotion ID must be positive") Long id) {
        logger.info("Fetching promotion with id: {}", id);
        PromotionDTO promotion = promotionService.getPromotion(id);
        return ResponseEntity.ok(ApiResponse.success("Promotion retrieved", promotion));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get a promotion by code", description = "Retrieves a promotion by its code.")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<PromotionDTO>> getPromotionByCode(
            @PathVariable @NotBlank(message = "Promotion code is required") String code) {
        logger.info("Fetching promotion with code: {}", code);
        PromotionDTO promotion = promotionService.getPromotionByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Promotion retrieved", promotion));
    }

    @GetMapping
    @Operation(summary = "Get all active promotions", description = "Retrieves all active promotions.")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<PromotionDTO>>> getAllPromotions() {
        logger.info("Fetching all active promotions");
        List<PromotionDTO> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(ApiResponse.success("Promotions retrieved", promotions));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a promotion", description = "Updates a specific promotion. Accessible to ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionDTO>> updatePromotion(
            @PathVariable @Positive(message = "Promotion ID must be positive") Long id,
            @Valid @RequestBody PromotionDTO promotionDTO,
            Authentication authentication) {
        logger.info("Updating promotion with id: {}", id);
        PromotionDTO updatedPromotion = promotionService.updatePromotion(id, promotionDTO, authentication);
        return ResponseEntity.ok(ApiResponse.success("Promotion updated", updatedPromotion));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a promotion", description = "Soft deletes a specific promotion. Accessible to ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
            @PathVariable @Positive(message = "Promotion ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Deleting promotion with id: {}", id);
        promotionService.deletePromotion(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Promotion deleted", null));
    }
}