package com.podStream.PodStream.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * Entidad que representa los detalles de los productos dentro de una orden de compra en PodStream.
 */
@Entity
@Table(name = "order_details")
@Data
public class Details {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Positive(message = "La cantidad debe ser positiva")
    @Column(nullable = false)
    private Integer quantity;

    @Positive(message = "El precio debe ser positivo")
    @Column(nullable = false)
    private Double price;

    private String description;

    @NotNull(message = "La orden de compra es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "active")
    private boolean active = true;

    public Details() {
    }

    public Details(String productName, int quantity, double price, String description, PurchaseOrder purchaseOrder, Product product) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.description = description;
        this.purchaseOrder = purchaseOrder;
        this.product = product;
        this.active = true;
    }
}