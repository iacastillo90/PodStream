package com.podStream.PodStream.Service;

import com.podStream.PodStream.Models.Cart;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Repositories.ProductRepository;
import com.podStream.PodStream.Services.CartService;
import com.podStream.PodStream.Services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    public void testAddItemToCart() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0);
        product.setStock(10);
        product = productRepository.save(product);

        String sessionId = UUID.randomUUID().toString();
        Cart cart = cartService.addItemToCart(product.getId(), 1, sessionId);

        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(1, cart.getItems().get(0).getQuantity());
        assertEquals(8, productRepository.findById(product.getId()).get().getStock());
    }
}
