package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.ColorProduct;
import com.podStream.PodStream.Models.Product;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para representar un producto en la API REST de PodStream.
 */
@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    private String name;

    private String description;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private double price;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;

    @Min(value = 0, message = "El contador de ventas no puede ser negativo")
    private int salesCount;

    private Long categoryId;

    private ColorProduct color;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Max(value = 1, message = "El descuento no puede exceder el 100%")
    private double discount;

    @Pattern(regexp = "^(http|https)://.*$", message = "La URL de la imagen debe ser válida")
    private String image;

    @PositiveOrZero(message = "La puntuación promedio no puede ser negativa")
    private double averageRating;

    @Size(max = 10, message = "Máximo 10 imágenes adicionales")
    private List<String> imageCollection = new ArrayList<>();

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ProductDTO() {}

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.salesCount = product.getSalesCount();
        this.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        this.color = product.getColor();
        this.discount = product.getDiscount();
        this.image = product.getImage();
        this.averageRating = product.getAverageRating();
        this.imageCollection = product.getImageCollection();
        this.active = product.isActive();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }
}