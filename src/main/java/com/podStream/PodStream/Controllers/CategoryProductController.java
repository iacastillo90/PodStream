package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.CategoryProduct;
import com.podStream.PodStream.Services.CategoryProductService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryProductController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryProductController.class);

    private final CategoryProductService categoryService;

    @GetMapping
    @Operation(summary = "Listar todas las categorías", description = "Obtiene todas las categorías disponibles.")
    public ResponseEntity<ApiResponse<List<CategoryProduct>>> getAllCategories() {
        logger.info("Fetching all categories");
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", categoryService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría por ID", description = "Obtiene los detalles de una categoría específica.")
    public ResponseEntity<ApiResponse<CategoryProduct>> getCategoryById(@PathVariable Long id) {
        logger.info("Fetching category with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved", categoryService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva categoría", description = "Crea una nueva categoría. Requiere rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryProduct>> createCategory(@Valid @RequestBody CategoryProduct category) {
        logger.info("Creating category: {}", category.getName());
        return new ResponseEntity<>(ApiResponse.success("Category created", categoryService.save(category)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría", description = "Actualiza los detalles de una categoría existente. Requiere rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryProduct>> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryProduct category) {
        logger.info("Updating category with id: {}", id);
        category.setId(id);
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.update(category)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría", description = "Elimina una categoría si no tiene productos asociados. Requiere rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        logger.info("Deleting category with id: {}", id);
        categoryService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
