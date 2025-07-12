package com.podStream.PodStream.Services.Implement;
import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.CartItemDTO;
import com.podStream.PodStream.Models.Cart;
import com.podStream.PodStream.Models.CartItem;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Repositories.Jpa.CartItemRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticCartItemRepository;
import com.podStream.PodStream.Repositories.Jpa.CartRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRepository;
import com.podStream.PodStream.Services.CartItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImplement implements CartItemService {

    private static final Logger logger = LoggerFactory.getLogger(CartItemServiceImplement.class);
    private static final String CART_ITEM_KEY_PREFIX = "cart:item:";
    private static final long CART_ITEM_TTL_MINUTES = 60; // 1 hora para ítems del carrito

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ElasticCartItemRepository searchRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PodStreamPrometheusConfig podStreamPrometheusConfig;

    @Override
    @Transactional
    public CartItemDTO addItemToCart(Long cartId, CartItemDTO itemDTO, Authentication authentication) {
        logger.info("Adding item to cart, cartId: {}, productId: {}", cartId, itemDTO.getProductId());
        Long clientId = validateAuthentication(authentication);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found: " + cartId));
        validateCartOwnership(cart, clientId);

        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemDTO.getProductId()));
        if (product.getStock() < itemDTO.getQuantity()) {
            podStreamPrometheusConfig.incrementCartErrors();
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }

        CartItem existingItem = cartItemRepository.findByCartIdAndProductIdAndActiveTrue(cartId, itemDTO.getProductId())
                .orElse(null);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + itemDTO.getQuantity());
            cartItemRepository.save(existingItem);
            searchRepository.save(existingItem);
            redisTemplate.opsForValue().set(CART_ITEM_KEY_PREFIX + existingItem.getId(), existingItem, CART_ITEM_TTL_MINUTES, TimeUnit.MINUTES);
            podStreamPrometheusConfig.incrementCartItemsUpdated();
            return new CartItemDTO(existingItem);
        }

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(itemDTO.getQuantity());
        cartItem.setActive(true);

        CartItem savedItem = cartItemRepository.save(cartItem);
        searchRepository.save(savedItem);
        redisTemplate.opsForValue().set(CART_ITEM_KEY_PREFIX + savedItem.getId(), savedItem, CART_ITEM_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementCartItemsAdded();
        return new CartItemDTO(savedItem);
    }

    @Override
    @Transactional
    public CartItemDTO updateItemQuantity(Long cartId, Long itemId, Integer quantity, Authentication authentication) {
        logger.info("Updating item quantity, cartId: {}, itemId: {}, quantity: {}", cartId, itemId, quantity);
        Long clientId = validateAuthentication(authentication);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found: " + cartId));
        validateCartOwnership(cart, clientId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));
        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new IllegalStateException("Cart item does not belong to cart: " + cartId);
        }

        Product product = cartItem.getProduct();
        if (product.getStock() < quantity) {
            podStreamPrometheusConfig.incrementCartErrors();
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }

        cartItem.setQuantity(quantity);
        CartItem updatedItem = cartItemRepository.save(cartItem);
        searchRepository.save(updatedItem);
        redisTemplate.opsForValue().set(CART_ITEM_KEY_PREFIX + updatedItem.getId(), updatedItem, CART_ITEM_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementCartItemsUpdated();
        return new CartItemDTO(updatedItem);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long cartId, Long itemId, Authentication authentication) {
        logger.info("Removing item from cart, cartId: {}, itemId: {}", cartId, itemId);
        Long clientId = validateAuthentication(authentication);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found: " + cartId));
        validateCartOwnership(cart, clientId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));
        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new IllegalStateException("Cart item does not belong to cart: " + cartId);
        }

        cartItem.setActive(false);
        cartItemRepository.save(cartItem);
        searchRepository.save(cartItem);
        redisTemplate.delete(CART_ITEM_KEY_PREFIX + itemId);

        podStreamPrometheusConfig.incrementCartItemsRemoved();
    }

    @Override
    public List<CartItemDTO> getCartItems(Long cartId, Authentication authentication) {
        logger.info("Fetching items for cart, cartId: {}", cartId);
        Long clientId = validateAuthentication(authentication);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found: " + cartId));
        validateCartOwnership(cart, clientId);

        return cartItemRepository.findByCartIdAndActiveTrue(cartId)
                .stream()
                .map(CartItemDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public CartItemDTO getCartItem(Long cartId, Long itemId, Authentication authentication) {
        logger.info("Fetching cart item, cartId: {}, itemId: {}", cartId, itemId);
        Long clientId = validateAuthentication(authentication);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found: " + cartId));
        validateCartOwnership(cart, clientId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));
        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new IllegalStateException("Cart item does not belong to cart: " + cartId);
        }

        return new CartItemDTO(cartItem);
    }

    private Long validateAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            throw new SecurityException("Authentication required");
        }
        return Long.valueOf(authentication.getName());
    }

    private void validateCartOwnership(Cart cart, Long clientId) {
        if (cart.getClient() != null && !cart.getClient().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to access cart: {}", clientId, cart.getId());
            throw new SecurityException("Not authorized to access this cart");
        }
    }
}
