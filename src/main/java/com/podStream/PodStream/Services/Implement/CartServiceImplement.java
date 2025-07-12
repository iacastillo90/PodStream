package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.CartDTO;
import com.podStream.PodStream.Models.Cart;
import com.podStream.PodStream.Models.CartItem;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.Promotion;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.*;
import com.podStream.PodStream.Repositories.Elastic.ElasticCartRepository;
import com.podStream.PodStream.Services.CartService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CartServiceImplement implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImplement.class);
    private static final String CART_KEY_PREFIX = "cart:session:";
    private static final long CART_TTL_MINUTES = 60 * 24; // 24 horas

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ElasticCartRepository elasticCartRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PodStreamPrometheusConfig podStreamPrometheusConfig;

    @Autowired
    private ClientRepository clientRepository;

    @Override
    @Transactional
    public CartDTO getOrCreateCart(String sessionId, Authentication authentication) {
        logger.info("Fetching or creating cart for sessionId: {}", sessionId);
        sessionId = validateSessionId(sessionId);
        Long clientId = extractClientId(authentication);

        Cart cart;
        if (clientId != null) {
            String finalSessionId = sessionId;
            cart = cartRepository.findByClientId(clientId)
                    .orElseGet(() -> createNewCart(clientId, finalSessionId));
        } else {
            String redisKey = CART_KEY_PREFIX + sessionId;
            cart = (Cart) redisTemplate.opsForValue().get(redisKey);
            if (cart == null) {
                cart = new Cart();
                cart.setSessionId(sessionId);
                cart.setActive(true);
                redisTemplate.opsForValue().set(redisKey, cart, CART_TTL_MINUTES, TimeUnit.MINUTES);
                logger.info("New cart created in Redis for sessionId: {}", sessionId);
            }
        }
        return new CartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO addItemToCart(Long productId, Integer quantity, String sessionId, Authentication authentication) {
        logger.info("Adding item to cart, productId: {}, quantity: {}", productId, quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        sessionId = validateSessionId(sessionId);
        Long clientId = extractClientId(authentication);
        Cart cart = getOrCreateCartInternal(sessionId, clientId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        if (product.getStock() < quantity) {
            podStreamPrometheusConfig.incrementCartErrors();
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        cart.setTotalPrice(calculateTotalAmount(cart));
        saveCart(cart, sessionId, clientId);
        podStreamPrometheusConfig.incrementCartItemsAdded();
        return new CartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO updateCartItem(Long itemId, Integer quantity, String sessionId, Authentication authentication) {
        logger.info("Updating cart item: {}, quantity: {}", itemId, quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        sessionId = validateSessionId(sessionId);
        Long clientId = extractClientId(authentication);
        Cart cart = getOrCreateCartInternal(sessionId, clientId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));

        validateCartOwnership(cart, clientId);

        Product product = cartItem.getProduct();
        int stockDifference = quantity - cartItem.getQuantity();
        if (product.getStock() < stockDifference) {
            podStreamPrometheusConfig.incrementCartErrors();
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }

        cartItem.setQuantity(quantity);
        product.setStock(product.getStock() - stockDifference);
        productRepository.save(product);
        cart.setTotalPrice(calculateTotalAmount(cart));
        saveCart(cart, sessionId, clientId);
        podStreamPrometheusConfig.incrementCartItemsUpdated();
        return new CartDTO(cart);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long itemId, String sessionId, Authentication authentication) {
        logger.info("Removing cart item: {}", itemId);
        sessionId = validateSessionId(sessionId);
        Long clientId = extractClientId(authentication);
        Cart cart = getOrCreateCartInternal(sessionId, clientId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));

        validateCartOwnership(cart, clientId);

        Product product = cartItem.getProduct();
        product.setStock(product.getStock() + cartItem.getQuantity());
        productRepository.save(product);
        cart.getItems().remove(cartItem);
        cart.setTotalPrice(calculateTotalAmount(cart));
        saveCart(cart, sessionId, clientId);
        podStreamPrometheusConfig.incrementCartItemsRemoved();
    }

    @Override
    @Transactional
    public void clearCart(String sessionId, Authentication authentication) {
        logger.info("Clearing cart for sessionId: {}", sessionId);
        sessionId = validateSessionId(sessionId);
        Long clientId = extractClientId(authentication);
        Cart cart = getOrCreateCartInternal(sessionId, clientId);

        validateCartOwnership(cart, clientId);

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cart.setDiscount(0.0);
        saveCart(cart, sessionId, clientId);
        podStreamPrometheusConfig.incrementCartCleared();
    }

    @Override
    @Transactional
    public void mergeCartOnLogin(String sessionId, Client client) {
        logger.info("Merging cart for sessionId: {} to client: {}", sessionId, client.getId());
        if (sessionId == null || sessionId.isEmpty()) {
            logger.warn("Attempt to merge with null or empty sessionId");
            return;
        }

        String redisKey = CART_KEY_PREFIX + sessionId;
        Cart sessionCart = (Cart) redisTemplate.opsForValue().get(redisKey);
        if (sessionCart == null || sessionCart.getItems().isEmpty()) {
            logger.info("No cart found in Redis for sessionId: {}", sessionId);
            return;
        }

        Cart userCart = cartRepository.findByClientId(client.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setClient(client);
                    newCart.setSessionId(sessionId);
                    newCart.setActive(true);
                    return cartRepository.save(newCart);
                });

        for (CartItem sessionItem : sessionCart.getItems()) {
            Long productId = sessionItem.getProduct().getId();
            Integer quantity = sessionItem.getQuantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
            if (product.getStock() < quantity) {
                podStreamPrometheusConfig.incrementCartErrors();
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            CartItem existingItem = userCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (existingItem == null) {
                CartItem newItem = new CartItem();
                newItem.setCart(userCart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                userCart.getItems().add(newItem);
            } else {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
        }

        userCart.setTotalPrice(calculateTotalAmount(userCart));
        cartRepository.save(userCart);
        elasticCartRepository.save(userCart);
        redisTemplate.delete(redisKey);
        logger.info("Cart merged from Redis to MySQL for client: {}", client.getId());
        podStreamPrometheusConfig.incrementCartMerged();
    }

    @Override
    @Transactional
    public CartDTO applyPromotion(String sessionId, String promotionCode, Authentication authentication) {
        logger.info("Applying promotion {} to cart for sessionId: {}", promotionCode, sessionId);
        sessionId = validateSessionId(sessionId);
        Long clientId = extractClientId(authentication);
        Cart cart = getOrCreateCartInternal(sessionId, clientId);

        validateCartOwnership(cart, clientId);

        Promotion promotion = promotionRepository.findByCodeAndActiveTrue(promotionCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or inactive promotion code"));
        if (promotion.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Promotion code expired");
        }

        cart.setDiscount(promotion.getDiscountPercentage());
        cart.setTotalPrice(calculateTotalAmount(cart) * (1 - promotion.getDiscountPercentage() / 100));
        saveCart(cart, sessionId, clientId);
        podStreamPrometheusConfig.incrementCartPromotionApplied();
        return new CartDTO(cart);
    }

    private Cart getOrCreateCartInternal(String sessionId, Long clientId) {
        Cart cart;
        if (clientId != null) {
            cart = cartRepository.findByClientId(clientId)
                    .orElseGet(() -> createNewCart(clientId, sessionId));
        } else {
            String redisKey = CART_KEY_PREFIX + sessionId;
            cart = (Cart) redisTemplate.opsForValue().get(redisKey);
            if (cart == null) {
                cart = new Cart();
                cart.setSessionId(sessionId);
                cart.setActive(true);
                redisTemplate.opsForValue().set(redisKey, cart, CART_TTL_MINUTES, TimeUnit.MINUTES);
            }
        }
        return cart;
    }

    private Cart createNewCart(Long clientId, String sessionId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));
        Cart newCart = new Cart();
        newCart.setClient(client);
        newCart.setSessionId(sessionId);
        newCart.setActive(true);
        return cartRepository.save(newCart);
    }

    private void saveCart(Cart cart, String sessionId, Long clientId) {
        cart.setTotalPrice(calculateTotalAmount(cart));
        if (clientId != null) {
            cartRepository.save(cart);
            elasticCartRepository.save(cart);
        } else {
            redisTemplate.opsForValue().set(CART_KEY_PREFIX + sessionId, cart, CART_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }

    private Double calculateTotalAmount(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    private String validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    private Long extractClientId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return Long.valueOf(authentication.getName());
        }
        return null;
    }

    private void validateCartOwnership(Cart cart, Long clientId) {
        if (clientId != null && cart.getClient() != null && !cart.getClient().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to modify cart id: {}", clientId, cart.getId());
            throw new SecurityException("Not authorized to modify this cart");
        }
    }


}
