package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.RecommendationResponseDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar recomendaciones de productos en PodStream.
 */
@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendation Management", description = "APIs for retrieving product recommendations in the PodStream e-commerce platform")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get personalized product recommendations for a user", description = "Retrieves a list of recommended products based on user interactions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No recommendations available for user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid user ID or parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<RecommendationResponseDTO>>> getRecommendationsForUser(
            @PathVariable @Positive(message = "User ID must be positive") Long userId,
            @RequestParam(defaultValue = "10") int howMany) {
        logger.info("Requesting recommendations for user ID: {} with {} items", userId, howMany);
        List<RecommendationResponseDTO> recommendations = recommendationService.getRecommendationsForUser(userId, howMany);
        if (recommendations.isEmpty()) {
            logger.debug("No recommendations found for user ID: {}", userId);
            return new ResponseEntity<>(ApiResponse.error("No recommendations found for user " + userId), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved", recommendations));
    }

    @GetMapping("/products/{productId}/content-based")
    @Operation(summary = "Get content-based product recommendations", description = "Retrieves recommendations based on the similarity of product attributes.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Content-based recommendations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found or no recommendations available"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid product ID or parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<RecommendationResponseDTO>>> getContentBasedRecommendations(
            @PathVariable @Positive(message = "Product ID must be positive") Long productId,
            @RequestParam(defaultValue = "10") int howMany) {
        logger.info("Requesting content-based recommendations for product ID: {} with {} items", productId, howMany);
        List<RecommendationResponseDTO> recommendations = recommendationService.getContentBasedRecommendations(productId, howMany);
        if (recommendations.isEmpty()) {
            logger.debug("No content-based recommendations found for product ID: {}", productId);
            return new ResponseEntity<>(ApiResponse.error("No content-based recommendations found for product " + productId), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(ApiResponse.success("Content-based recommendations retrieved", recommendations));
    }
}