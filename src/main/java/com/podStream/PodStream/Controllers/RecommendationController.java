
package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.ClientInteractionRequest;
import com.podStream.PodStream.DTOS.RecommendationResponseDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.ClientInteraction;
import com.podStream.PodStream.Services.ClientInteractionService;
import com.podStream.PodStream.Services.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar interacciones de usuarios y recomendaciones de productos en PodStream.
 * <p>Proporciona endpoints para registrar interacciones (como vistas o compras) y obtener recomendaciones
 * personalizadas o basadas en contenido.
 *
 * @author [Tu Nombre o Equipo PodStream]
 * @since 0.0.1-SNAPSHOT
 */

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final ClientInteractionService clientInteractionService;
    private final RecommendationService recommendationService;
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);


/**
     * Registra una interacción de un usuario con un producto.
     * <p>Las interacciones (como vistas, compras, o calificaciones) se usan para alimentar
     * el motor de recomendaciones.
     *
     * @param request DTO con los detalles de la interacción.
     * @return Respuesta con la interacción registrada.
     */

    @PostMapping("/interactions")
    @Operation(summary = "Record a user interaction with a product", description = "Records user actions like view, add to cart, or purchase for recommendation engine.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interaction recorded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client or product not found")
    })
    public ResponseEntity<ApiResponse<ClientInteraction>> recordClientInteraction(@Valid @RequestBody ClientInteractionRequest request) {
        logger.info("Received user interaction request for user {} and product {}", request.getUserId(), request.getProductId());
        ClientInteraction interaction = clientInteractionService.recordInteraction(request);
        return ResponseEntity.ok(ApiResponse.success("Interaction recorded", interaction));
    }


/**
     * Obtiene recomendaciones personalizadas para un usuario basadas en sus interacciones.
     *
     * @param userId Identificador del usuario.
     * @param howMany Número máximo de recomendaciones a devolver (por defecto 10).
     * @return Lista de recomendaciones o error si no se encuentran.
     */

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get personalized product recommendations for a user", description = "Retrieves a list of recommended products based on user interactions.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No recommendations available for user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid user ID or parameters")
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


/**
     * Obtiene recomendaciones basadas en el contenido para un producto específico.
     * <p>Las recomendaciones se generan según la similitud de atributos del producto (categoría, color, etc.).
     *
     * @param productId Identificador del producto.
     * @param howMany Número máximo de recomendaciones a devolver (por defecto 10).
     * @return Lista de recomendaciones o error si no se encuentran.
     */

    @GetMapping("/products/{productId}/content-based")
    @Operation(summary = "Get content-based product recommendations", description = "Retrieves recommendations based on the similarity of product attributes.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Content-based recommendations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found or no recommendations available"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid product ID or parameters")
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
