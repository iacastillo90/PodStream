package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.ProductDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar productos en PodStream.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "APIs for managing products in the PodStream platform")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product. Accessible to ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @RequestBody ProductDTO productDTO,
            Authentication authentication) {
        logger.info("Creating product: {}", productDTO.getName());
        ProductDTO savedProduct = productService.createProduct(productDTO, authentication);
        return new ResponseEntity<>(ApiResponse.success("Product created", savedProduct), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID", description = "Retrieves a specific product.")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(
            @PathVariable @Positive(message = "Product ID must be positive") Long id) {
        logger.info("Fetching product with id: {}", id);
        ProductDTO product = productService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", product));
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all active products.")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        logger.info("Fetching all products");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name", description = "Searches products by name.")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByName(
            @RequestParam String name) {
        logger.info("Searching products by name: {}", name);
        List<ProductDTO> products = productService.getProductsByName(name);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieves products by category ID.")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(
            @PathVariable @Positive(message = "Category ID must be positive") Long categoryId) {
        logger.info("Fetching products by category: {}", categoryId);
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @GetMapping("/price")
    @Operation(summary = "Get products by price range", description = "Retrieves products within a price range.")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByPriceRange(
            @RequestParam @PositiveOrZero(message = "Min price must be non-negative") double minPrice,
            @RequestParam @PositiveOrZero(message = "Max price must be non-negative") double maxPrice) {
        logger.info("Fetching products by price range: {}-{}", minPrice, maxPrice);
        List<ProductDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get top popular products", description = "Retrieves top popular products by sales count.")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getTopPopularProducts(
            @RequestParam(defaultValue = "10") @Positive(message = "Limit must be positive") int limit) {
        logger.info("Fetching top {} popular products", limit);
        List<ProductDTO> products = productService.getTopPopularProducts(limit);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "Updates a specific product. Accessible to ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable @Positive(message = "Product ID must be positive") Long id,
            @RequestBody ProductDTO productDTO,
            Authentication authentication) {
        logger.info("Updating product with id: {}", id);
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO, authentication);
        return ResponseEntity.ok(ApiResponse.success("Product updated", updatedProduct));
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "Update product stock", description = "Updates the stock of a specific product. Accessible to ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateStock(
            @PathVariable @Positive(message = "Product ID must be positive") Long id,
            @RequestParam @PositiveOrZero(message = "Stock must be non-negative") Integer newStock,
            @RequestParam String updatedBy,
            Authentication authentication) {
        logger.info("Updating stock for product id: {}", id);
        ProductDTO updatedProduct = productService.updateStock(id, newStock, updatedBy, authentication);
        return ResponseEntity.ok(ApiResponse.success("Stock updated", updatedProduct));
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "Add a product rating", description = "Adds a rating to a specific product. Accessible to USER.")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> addRating(
            @PathVariable @Positive(message = "Product ID must be positive") Long id,
            @RequestParam @Min(value = 1, message = "Rating must be at least 1") @Max(value = 5, message = "Rating cannot exceed 5") int rating,
            @RequestParam @Positive(message = "Client ID must be positive") Long clientId,
            Authentication authentication) {
        logger.info("Adding rating for product id: {}", id);
        productService.addRating(id, rating, clientId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Rating added", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Soft deletes a specific product. Accessible to ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable @Positive(message = "Product ID must be positive") Long id,
            Authentication authentication) {
        logger.info("Deleting product with id: {}", id);
        productService.deleteProduct(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }
}