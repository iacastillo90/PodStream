package com.podStream.PodStream.Controllers;


import com.podStream.PodStream.Models.Cart;
import com.podStream.PodStream.Services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @GetMapping
    @Operation(summary = "Obtener el carrito actual")
    public ResponseEntity<Cart> getCart(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        try {
            Cart cart = cartService.getOrCreateCart(sessionId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            logger.error("Error al obtener el carrito: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar un item al carrito")
    public ResponseEntity<Cart> addItemToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        try {
            Cart cart = cartService.addItemToCart(productId, quantity, sessionId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            logger.error("Error al agregar el item al carrito: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Actualizar la cantidad de un item en el carrito")
    public ResponseEntity<Cart> updateCartItem(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        try {
            Cart cart = cartService.updateCartItem(itemId, quantity, sessionId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            logger.error("Error al actualizar el item en el carrito: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Eliminar un item del carrito")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        try {
            cartService.removeItemFromCart(itemId, sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error al eliminar el item del carrito: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    @Operation(summary = "Vaciar el carrito")
    public ResponseEntity<Void> clearCart(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        try {
            cartService.clearCart(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error al vaciar el carrito: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }








}
