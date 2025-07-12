package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.Cart;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartDTO {

    private Long id;
    private Long clientId;
    private String sessionId;
    private List<CartItemDTO> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PositiveOrZero(message = "Total price must be zero or positive")
    private Double totalPrice;

    @PositiveOrZero(message = "Discount must be zero or positive")
    private Double discount;

    private boolean active;

    public CartDTO() {}

    public CartDTO(Cart cart) {
        this.id = cart.getId();
        this.clientId = cart.getClient() != null ? cart.getClient().getId() : null;
        this.sessionId = cart.getSessionId();
        this.items = cart.getItems().stream().map(CartItemDTO::new).collect(Collectors.toList());
        this.createdAt = cart.getCreatedAt();
        this.updatedAt = cart.getUpdatedAt();
        this.totalPrice = cart.getTotalPrice();
        this.discount = cart.getDiscount();
        this.active = cart.isActive();
    }

    public Cart toEntity() {
        Cart cart = new Cart();
        cart.setId(this.id);
        cart.setSessionId(this.sessionId);
        cart.setTotalPrice(this.totalPrice);
        cart.setDiscount(this.discount);
        cart.setActive(this.active);
        return cart;
    }
}
