package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.CartItem;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO para representar un ítem del carrito en PodStream.
 */
@Data
public class CartItemDTO {

    private Long id;

    @Positive(message = "Cart ID must be positive")
    private Long cartId;

    @Positive(message = "Product ID must be positive")
    private Long productId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private Double price;

    public CartItemDTO() {}

    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.cartId = cartItem.getCart().getId();
        this.productId = cartItem.getProduct().getId();
        this.quantity = cartItem.getQuantity();
        this.price = cartItem.getProduct().getPrice();
    }

}
