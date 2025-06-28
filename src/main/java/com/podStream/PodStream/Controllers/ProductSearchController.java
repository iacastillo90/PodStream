package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.Models.CategoryProduct;
import com.podStream.PodStream.Models.ProductDocument;
import com.podStream.PodStream.Services.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class ProductSearchController {
    private static final Logger logger = LoggerFactory.getLogger(ProductSearchController.class);

    @Autowired
    private ProductSearchService productSearchService;

    @GetMapping("/products")
    @Operation(summary = "Buscar productos por nombre o descripción")
    public ResponseEntity<List<ProductDocument>> searchProducts(@RequestParam String query) {
        try {
            List<ProductDocument> results = productSearchService.searchProducts(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error al buscar productos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/products/filter")
    @Operation(summary = "Filtrar productos por categoría y precio")
    public ResponseEntity<List<ProductDocument>> filterProducts(
            @RequestParam CategoryProduct category,
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        try {
            List<ProductDocument> results = productSearchService.filterProducts(category, minPrice, maxPrice);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error al filtrar productos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
