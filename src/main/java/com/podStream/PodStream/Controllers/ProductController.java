package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @PutMapping("/{id}/stock")
    @Operation(summary = "Actualizar el stock de un producto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long id,
            @RequestParam Integer newStock,
            @RequestParam String updatedBy) {
        try {
            Product updatedProduct = productService.updateStock(id, newStock, updatedBy);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            logger.error("Error al actualizar el stock del producto {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.getProduct(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            logger.error("Error al obtener el producto {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.findAll()));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.save(product);
        return new ResponseEntity<>(ApiResponse.success("Product created successfully", savedProduct),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully",
                productService.update(product)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

}