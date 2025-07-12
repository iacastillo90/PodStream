package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.ProductDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.Implement.ProductSearchServiceImplement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para búsqueda y filtrado de productos en PodStream.
 */
@RestController
@RequestMapping("/api/search")
@Tag(name = "Product Search", description = "APIs for searching and filtering products in the PodStream platform")
public class ProductSearchController {

    private static final Logger logger = LoggerFactory.getLogger(ProductSearchController.class);

    private final ProductSearchServiceImplement productSearchServiceImplement;

    public ProductSearchController(ProductSearchServiceImplement productSearchServiceImplement) {
        this.productSearchServiceImplement = productSearchServiceImplement;
    }

    @GetMapping("/products")
    @Operation(summary = "Search products by name or description", description = "Searches active products by name or description.")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @RequestParam @NotBlank(message = "Query cannot be empty") String query) {
        logger.info("Searching products with query: {}", query);
        List<ProductDTO> results = productSearchServiceImplement.searchProducts(query);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", results));
    }

    @GetMapping("/products/filter")
    @Operation(summary = "Filter products by category and price", description = "Filters active products by category ID and price range.")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> filterProducts(
            @RequestParam @Positive(message = "Category ID must be positive") Long categoryId,
            @RequestParam @PositiveOrZero(message = "Min price must be non-negative") Double minPrice,
            @RequestParam @PositiveOrZero(message = "Max price must be non-negative") Double maxPrice) {
        logger.info("Filtering products by category ID: {}, price: {}-{}", categoryId, minPrice, maxPrice);
        List<ProductDTO> results = productSearchServiceImplement.filterProducts(categoryId, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", results));
    }
}