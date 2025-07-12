package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.CategoryProductDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.CategoryProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "APIs for managing product categories in the PodStream e-commerce platform")
@RequiredArgsConstructor
public class CategoryProductController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryProductController.class);

    private final CategoryProductService categoryService;

    @GetMapping
    @Operation(summary = "List all categories", description = "Retrieves all active product categories.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<CategoryProductDTO>>> getAllCategories(Authentication authentication) {
        logger.info("Fetching all categories");
        List<CategoryProductDTO> categories = categoryService.findAll(authentication);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CategoryProductDTO>> getCategoryById(@PathVariable Long id, Authentication authentication) {
        logger.info("Fetching category with id: {}", id);
        CategoryProductDTO category = categoryService.findById(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved", category));
    }

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new product category. Requires ADMIN role.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category name already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryProductDTO>> createCategory(
            @Valid @RequestBody CategoryProductDTO categoryDTO,
            Authentication authentication) {
        logger.info("Creating category: {}", categoryDTO.getName());
        CategoryProductDTO createdCategory = categoryService.save(categoryDTO, authentication);
        return new ResponseEntity<>(ApiResponse.success("Category created", createdCategory), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Updates an existing product category. Requires ADMIN role.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category name already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryProductDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryProductDTO categoryDTO,
            Authentication authentication) {
        logger.info("Updating category with id: {}", id);
        CategoryProductDTO updatedCategory = categoryService.update(id, categoryDTO, authentication);
        return ResponseEntity.ok(ApiResponse.success("Category updated", updatedCategory));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Soft deletes a category if it has no associated products. Requires ADMIN role.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category has associated products"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id, Authentication authentication) {
        logger.info("Deleting category with id: {}", id);
        categoryService.deleteById(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}