package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.CartDTO;
import com.podStream.PodStream.Models.Cart;
import com.podStream.PodStream.Models.User.Client;
import org.springframework.security.core.Authentication;

public interface CartService {

    CartDTO getOrCreateCart(String sessionId, Authentication authentication);
    CartDTO addItemToCart(Long productId, Integer quantity, String sessionId, Authentication authentication);
    CartDTO updateCartItem(Long itemId, Integer quantity, String sessionId, Authentication authentication);
    void removeItemFromCart(Long itemId, String sessionId, Authentication authentication);
    void clearCart(String sessionId, Authentication authentication);
    void mergeCartOnLogin(String sessionId, Client client);
    CartDTO applyPromotion(String sessionId, String promotionCode, Authentication authentication);
}
