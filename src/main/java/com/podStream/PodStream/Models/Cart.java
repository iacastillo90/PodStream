package com.podStream.PodStream.Models;

import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name= "carts")
@Data
@Document(indexName = "carts")
@EntityListeners(AuditingEntityListener.class)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total_price")
    private Double totalPrice = 0.0;

    @Column(name = "Discount")
    private Double discount = 0.0;

    @Column(name = "active")
    private boolean active = true;

    public Cart() { }

    public Cart(Long id, Client client, String sessionId, List<CartItem> items, LocalDateTime createdAt, LocalDateTime updatedAt, Double totalPrice, Double discount) {
        this.id = id;
        this.client = client;
        this.sessionId = sessionId;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalPrice = totalPrice;
        this.discount = discount;
    }

}
