package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Models.Cart;
import com.podStream.PodStream.Models.CartItem;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.Promotion;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.CartItemRepository;
import com.podStream.PodStream.Repositories.CartRepository;
import com.podStream.PodStream.Repositories.ProductRepository;
import com.podStream.PodStream.Repositories.PromotionRepository;
import com.podStream.PodStream.Services.CartService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CartServiceImplement implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private PromotionRepository promotionRepository;

    private static final String CART_KEY_PREFIX = "cart:session:";
    private static final long CART_TTL_MINUTES = 60 * 24; // 24 horas

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImplement.class);

    @Override
    @Transactional
    public Cart getOrCreateCart (String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        Client authenticatedUser = null;
        try {
            authenticatedUser = (Client) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            logger.error("Error al obtener el usuario autenticado: {}", e.getMessage());
        }

        Cart cart;

        if(authenticatedUser != null){
            //Usuario autenticado: Buscar o crear carrito
            Client finalAuthenticatedUser = authenticatedUser;
            String finalSessionId = sessionId;
            cart = cartRepository.findByClientId(authenticatedUser.getId())
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setClient(finalAuthenticatedUser);
                        newCart.setSessionId(finalSessionId);
                        return cartRepository.save(newCart);
                    });
        } else {
            // Usuario no autenticado: buscar en Redis o crear nuevo
            String redisKey = CART_KEY_PREFIX + sessionId;
            cart = (Cart) redisTemplate.opsForValue().get(redisKey);
            if (cart == null) {
                cart =new Cart();
                cart.setSessionId(sessionId);
                redisTemplate.opsForValue().set(redisKey, cart, CART_TTL_MINUTES, TimeUnit.MINUTES);
                logger.info("Nuevo carrito creado en Redis para sessionId? {}",sessionId);
            }
        }
        return cart;
    }

    @Override
    @Transactional
    public Cart addItemToCart (Long productId, Integer quantity, String sessionId) {
        if (quantity <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productId));

        if (product.getStock() < quantity) {
            meterRegistry.counter("cart.errors", "type", "insufficient_stock").increment();
            throw new IllegalStateException("Stock insuficiente para el producto: " + product.getName());
        }

        Cart cart = getOrCreateCart(sessionId);
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

        // Actualizar stock en el producto
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        // Guardar carrito
        if (cart.getClient() != null) {
            cart = cartRepository.save(cart);
        } else {
            redisTemplate.opsForValue().set(CART_KEY_PREFIX + sessionId, cart, CART_TTL_MINUTES, TimeUnit.MINUTES);
        }

        logger.info("Producto {} anadido al carrito, cantidad: {}",product.getName(), quantity);
        meterRegistry.counter("cart.items.added").increment();
        return cart;
    }

    @Override
    @Transactional
    public Cart updateCartItem (Long itemId, Integer quantity, String sessionId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }

        Cart cart = getOrCreateCart(sessionId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado en el carrito: " + itemId));

        Product product = cartItem.getProduct();
        int oldQuantity = cartItem.getQuantity();
        int stockDifference = quantity - oldQuantity;

        if (product.getStock() < stockDifference) {
            meterRegistry.counter("cart.errors", "type", "insufficient_stock").increment();
            throw new IllegalStateException("Stock insuficiente para el producto: " + product.getName());
        }

        cartItem.setQuantity(quantity);
        product.setStock(product.getStock() - stockDifference);

        if (product.getStock() < quantity) {
            meterRegistry.counter("cart.errors", "type", "insufficient_stock").increment();
            throw new IllegalStateException("Stock insuficiente para el producto: " + product.getName());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        if (cart.getClient() != null) {
            cart = cartRepository.save(cart);
        } else {
            redisTemplate.opsForValue().set(CART_KEY_PREFIX + sessionId, cart, CART_TTL_MINUTES, TimeUnit.MINUTES);
        }

        logger.info("Item {} actualizado en el carrito, nueva cantidad: {}", itemId, quantity);
        meterRegistry.counter("cart.item.update").increment();
        return cart;
    }

    @Override
    @Transactional
    public void removeItemFromCart (Long itemId, String sessionId) {
        Cart cart = getOrCreateCart(sessionId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado en el carrito: "));

        Product product = cartItem.getProduct();
        product.setStock(product.getStock() + cartItem.getQuantity());
        productRepository.save(product);

        cart.getItems().remove(cartItem);

        if (cart.getClient() != null) {
            cartRepository.save(cart);
        } else {
            redisTemplate.opsForValue().set(CART_KEY_PREFIX + sessionId, cart, CART_TTL_MINUTES, TimeUnit.MINUTES);
        }

        logger.info("Item {} eliminado del carrito", itemId);
        meterRegistry.counter("cart.items.removed").increment();
    }

    @Override
    @Transactional
    public void clearCart (String sessionId) {
        Cart cart = getOrCreateCart(sessionId);
        for(CartItem item : cart.getItems()){
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        cart.getItems().clear();

        if (cart.getClient() != null) {
            cartRepository.save(cart);
        } else {
            redisTemplate.delete(CART_KEY_PREFIX + sessionId);
        }

        logger.info("Carrito vaciado para sessionId: {}", sessionId);
        meterRegistry.counter("cart.cleared").increment();
    }

    @Override
    @Transactional
    public void mergeCartOnLogin(String sessionId, Client client) {
        if (sessionId == null || sessionId.isEmpty()) {
            logger.warn("Intento de merge con sessionId nulo o vacío");
            return;
        }

        String redisKey = CART_KEY_PREFIX + sessionId;
        Cart sessionCart = (Cart) redisTemplate.opsForValue().get(redisKey);
        if (sessionCart == null || sessionCart.getItems().isEmpty()) {
            logger.info("No hay carrito en Redis para sessionId: {}", sessionId);
            return;
        }

        // Obtener o crear el carrito del usuario autenticado
        Cart userCart = cartRepository.findByClientId(client.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setClient(client);
                    newCart.setSessionId(sessionId);
                    return cartRepository.save(newCart);
                });

        // Transferir ítems del carrito de Redis al carrito de MySQL
        for (CartItem sessionItem : sessionCart.getItems()) {
            Long productId = sessionItem.getProduct().getId();
            Integer quantity = sessionItem.getQuantity();

            // Verificar stock antes de transferir
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productId));
            if (product.getStock() < quantity) {
                meterRegistry.counter("cart.errors", "type", "insufficient_stock").increment();
                throw new IllegalStateException("Stock insuficiente para el producto: " + product.getName());
            }

            // Buscar si el producto ya está en el carrito del usuario
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

            // Actualizar stock
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
        }

        // Guardar el carrito actualizado en MySQL
        cartRepository.save(userCart);

        // Eliminar el carrito de Redis
        redisTemplate.delete(redisKey);
        logger.info("Carrito de Redis (sessionId: {}) transferido a MySQL para el usuario {}", sessionId, client.getId());
        meterRegistry.counter("cart.merged").increment();
    }

    public Cart applyPromotion(String sessionId, String promotionCode) {
        Cart cart = getOrCreateCart(sessionId);
        Promotion promotion = promotionRepository.findByCodeAndActiveTrue(promotionCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or inactive promotion code"));
        if (promotion.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Promotion code expired");
        }
        cart.setDiscount(promotion.getDiscountPercentage());
        cart.setTotalPrice(calculateTotalAmount(cart) * (1 - promotion.getDiscountPercentage() / 100));
        return cartRepository.save(cart);
    }

    private Double calculateTotalAmount(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }


}
