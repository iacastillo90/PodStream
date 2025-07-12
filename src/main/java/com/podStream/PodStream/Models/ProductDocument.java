package com.podStream.PodStream.Models;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import jakarta.persistence.Id;

/**
 * Documento Elasticsearch para representar un producto en búsquedas rápidas.
 */
@Data
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Integer)
    private Integer stock;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String color;

    @Field(type = FieldType.Double)
    private Double discount;

    @Field(type = FieldType.Double)
    private Double averageRating;

    @Field(type = FieldType.Boolean)
    private boolean active;

    public ProductDocument() {}

    public ProductDocument(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        this.color = product.getColor() != null ? product.getColor().name() : null;
        this.discount = product.getDiscount();
        this.averageRating = product.getAverageRating();
        this.active = product.isActive();
    }
}