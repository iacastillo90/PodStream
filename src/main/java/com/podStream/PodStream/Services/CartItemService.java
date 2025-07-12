package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.CartItemDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CartItemService {

    CartItemDTO addItemToCart(Long cartId, CartItemDTO itemDTO, Authentication authentication);
    CartItemDTO updateItemQuantity(Long cartId, Long itemId, Integer quantity, Authentication authentication);
    void removeItemFromCart(Long cartId, Long itemId, Authentication authentication);
    List<CartItemDTO> getCartItems(Long cartId, Authentication authentication);
    CartItemDTO getCartItem(Long cartId, Long itemId, Authentication authentication);

}
