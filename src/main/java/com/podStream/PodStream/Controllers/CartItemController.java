package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.CartItemDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.CartItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts/{cartId}/items")
@Tag(name = "Cart Item Management", description = "APIs for managing cart items in the PodStream e-commerce platform")
public class CartItemController {

    private static final Logger logger = LoggerFactory.getLogger(CartItemController.class);

    @Autowired
    private CartItemService cartItemService;

    @PostMapping
    @Operation(summary = "Add item to cart", description = "Adds a new item to the specified cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<CartItemDTO>> addItem(
            @PathVariable Long cartId,
            @Valid @RequestBody CartItemDTO itemDTO,
            Authentication authentication) {
        logger.info("Adding item to cart, cartId: {}, productId: {}", cartId, itemDTO.getProductId());
        CartItemDTO addedItem = cartItemService.addItemToCart(cartId, itemDTO, authentication);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", addedItem));
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of a cart item.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item quantity updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart or item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        logger.info("Updating item quantity, cartId: {}, itemId: {}, quantity: {}", cartId, itemId, quantity);
        CartItemDTO updatedItem = cartItemService.updateItemQuantity(cartId, itemId, quantity, authentication);
        return ResponseEntity.ok(ApiResponse.success("Item quantity updated", updatedItem));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the specified cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart or item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            Authentication authentication) {
        logger.info("Removing item from cart, cartId: {}, itemId: {}", cartId, itemId);
        cartItemService.removeItemFromCart(cartId, itemId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }

    @GetMapping
    @Operation(summary = "Get all items in cart", description = "Retrieves all items in the specified cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Items retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCartItems(
            @PathVariable Long cartId,
            Authentication authentication) {
        logger.info("Fetching items for cart, cartId: {}", cartId);
        List<CartItemDTO> items = cartItemService.getCartItems(cartId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Cart items retrieved", items));
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "Get cart item by ID", description = "Retrieves a specific item from the specified cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart or item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<CartItemDTO>> getCartItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            Authentication authentication) {
        logger.info("Fetching cart item, cartId: {}, itemId: {}", cartId, itemId);
        CartItemDTO item = cartItemService.getCartItem(cartId, itemId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Cart item retrieved", item));
    }
}