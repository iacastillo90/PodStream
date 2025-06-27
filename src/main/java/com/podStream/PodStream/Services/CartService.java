package com.podStream.PodStream.Services;

import com.podStream.PodStream.Models.Cart;
import com.podStream.PodStream.Models.User.Client;

public interface CartService {

    Cart getOrCreateCart (String sessionId);

    Cart addItemToCart (Long productId, Integer quantity, String sessionId);

    Cart updateCartItem (Long itemId, Integer quantity, String sessionId);

    void removeItemFromCart (Long itemId, String sessionId);

    void clearCart (String sessionId);

    void mergeCartOnLogin(String sessionId, Client client);
}
