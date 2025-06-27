
package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.User.Client;

import java.time.LocalDateTime;

public class ProductRatingDTO {

    private Long id;

    private Client client;

    private Product product;

    private Double rating;

    private LocalDateTime timestamp;

    public ProductRatingDTO() { }

    public ProductRatingDTO(Long id, Client client, Product product, Double rating, LocalDateTime timestamp) {
        this.id = id;
        this.client = client;
        this.product = product;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Double getRating() {
        return rating;
    }

    public Product getProduct() {
        return product;
    }

    public Client getClient() {
        return client;
    }

    public Long getId() {
        return id;
    }
}
