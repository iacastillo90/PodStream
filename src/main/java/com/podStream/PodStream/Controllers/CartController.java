package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.CartDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.ClientRepository;
import com.podStream.PodStream.Services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart Management", description = "APIs for managing shopping carts in the PodStream e-commerce platform")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping
    @Operation(summary = "Get or create cart", description = "Retrieves or creates a cart based on session ID or authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid session ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@RequestHeader(value = "X-Session-Id", required = false) String sessionId, Authentication authentication) {
        logger.info("Fetching cart for sessionId: {}", sessionId);
        CartDTO cart = cartService.getOrCreateCart(sessionId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a product to the cart with the specified quantity.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @RequestParam @Positive Long productId,
            @RequestParam @Positive Integer quantity,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Authentication authentication) {
        logger.info("Adding item to cart, productId: {}, quantity: {}", productId, quantity);
        CartDTO cart = cartService.addItemToCart(productId, quantity, sessionId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity", description = "Updates the quantity of an item in the cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @PathVariable Long itemId,
            @RequestParam @Positive Integer quantity,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Authentication authentication) {
        logger.info("Updating cart item: {}, quantity: {}", itemId, quantity);
        CartDTO cart = cartService.updateCartItem(itemId, quantity, sessionId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Item updated in cart", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeItemFromCart(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Authentication authentication) {
        logger.info("Removing item from cart: {}", itemId);
        cartService.removeItemFromCart(itemId, sessionId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Clears all items from the cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Authentication authentication) {
        logger.info("Clearing cart for sessionId: {}", sessionId);
        cartService.clearCart(sessionId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge cart on login", description = "Merges a session-based cart with the authenticated user's cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart merged successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid session ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Void>> mergeCartOnLogin(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Authentication authentication) {
        logger.info("Merging cart for sessionId: {}", sessionId);
        Long clientId = Long.valueOf(authentication.getName());
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));
        cartService.mergeCartOnLogin(sessionId, client);
        return ResponseEntity.ok(ApiResponse.success("Cart merged successfully", null));
    }

    @PostMapping("/promotion")
    @Operation(summary = "Apply promotion to cart", description = "Applies a promotion code to the cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Promotion applied successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired promotion code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CartDTO>> applyPromotion(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestParam String promotionCode,
            Authentication authentication) {
        logger.info("Applying promotion {} to cart for sessionId: {}", promotionCode, sessionId);
        CartDTO cart = cartService.applyPromotion(sessionId, promotionCode, authentication);
        return ResponseEntity.ok(ApiResponse.success("Promotion applied to cart", cart));
    }


}
