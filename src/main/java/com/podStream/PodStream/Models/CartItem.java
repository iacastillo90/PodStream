package com.podStream.PodStream.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "cart_items")
@Data
@Document(indexName = "cart_items")
@EntityListeners(AuditingEntityListener.class)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "active")
    private boolean active = true;

    public CartItem() {
    }

    public CartItem(Long id, Cart cart, Product product, Integer quantity) {
        this.id = id;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

}
