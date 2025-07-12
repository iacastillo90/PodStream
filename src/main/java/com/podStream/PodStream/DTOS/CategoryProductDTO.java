package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.CategoryProduct;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar una categoría de productos en PodStream.
 */
@Data
public class CategoryProductDTO {

    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    private boolean active;

    public CategoryProductDTO() {}

    public CategoryProductDTO(CategoryProduct category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.createdDate = category.getCreatedDate();
        this.lastModifiedDate = category.getLastModifiedDate();
        this.active = category.isActive();
    }
}
