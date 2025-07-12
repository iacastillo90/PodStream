package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.PurchaseOrderDTO;
import com.podStream.PodStream.Models.*;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.*;
import com.podStream.PodStream.Repositories.Elastic.ElasticPurchaseOrderRepository;
import com.podStream.PodStream.Services.CartService;
import com.podStream.PodStream.Services.OrderStatusHistoryService;
import com.podStream.PodStream.Services.PDFService;
import com.podStream.PodStream.Services.PurchaseOrderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar órdenes de compra en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@Service
public class PurchaseOrderServiceImplement implements PurchaseOrderService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderServiceImplement.class);
    private static final String ORDER_KEY_PREFIX = "order:";
    private static final long ORDER_TTL_MINUTES = 60; // 1 hora

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final DetailsRepository detailsRepository;
    private final CartService cartService;
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ElasticPurchaseOrderRepository searchRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;
    private final PDFService pdfService;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;
    private final OrderStatusHistoryService historyService;

    public PurchaseOrderServiceImplement(
            PurchaseOrderRepository purchaseOrderRepository,
            DetailsRepository detailsRepository,
            CartService cartService,
            ClientRepository clientRepository,
            AddressRepository addressRepository,
            ProductRepository productRepository,
            ElasticPurchaseOrderRepository searchRepository,
            RedisTemplate<String, Object> redisTemplate,
            JavaMailSender mailSender,
            PDFService pdfService,
            PodStreamPrometheusConfig podStreamPrometheusConfig,
            OrderStatusHistoryService historyService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.detailsRepository = detailsRepository;
        this.cartService = cartService;
        this.clientRepository = clientRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.searchRepository = searchRepository;
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
        this.pdfService = pdfService;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
        this.historyService = historyService;
    }

    @Override
    @Transactional
    public PurchaseOrderDTO createPurchaseOrder(PurchaseOrderDTO orderDTO, Authentication authentication) {
        logger.info("Creating purchase order, ticket: {}", orderDTO.getTicket());
        validateAuthentication(authentication, "ROLE_CLIENT");

        Client client = clientRepository.findById(orderDTO.getClientId())
                .filter(Client::getActive)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + orderDTO.getClientId()));
        Address address = addressRepository.findById(orderDTO.getAddressId())
                .filter(Address::getUsable)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + orderDTO.getAddressId()));

        if (purchaseOrderRepository.findByTicketAndActiveTrue(orderDTO.getTicket()).isPresent()) {
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new IllegalArgumentException("Ticket already exists");
        }

        validateOrderDTO(orderDTO);

        PurchaseOrder order = new PurchaseOrder();
        order.setTicket(orderDTO.getTicket());
        order.setAmount(orderDTO.getAmount());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setAddress(address);
        order.setClient(client);
        order.setCustomerRut(orderDTO.getCustomerRut());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setActive(true);

        Set<Details> details = orderDTO.getDetails().stream().map(dto -> {
            Product product = productRepository.findById(dto.getProductId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + dto.getProductId()));
            if (product.getStock() < dto.getQuantity()) {
                podStreamPrometheusConfig.incrementOrderErrors();
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
            product.setStock(product.getStock() - dto.getQuantity());
            productRepository.save(product);
            Details detail = new Details();
            detail.setPurchaseOrder(order);
            detail.setProduct(product);
            detail.setQuantity(dto.getQuantity());
            detail.setPrice(dto.getPrice());
            return detail;
        }).collect(Collectors.toSet());
        order.setDetails(details);

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        historyService.createHistory(savedOrder.getId(), null, OrderStatus.PENDING_PAYMENT, authentication);
        searchRepository.save(savedOrder);
        redisTemplate.opsForValue().set(ORDER_KEY_PREFIX + savedOrder.getId(), savedOrder, ORDER_TTL_MINUTES, TimeUnit.MINUTES);

        sendInvoiceEmail(savedOrder);
        podStreamPrometheusConfig.incrementOrderCreated();
        return new PurchaseOrderDTO(savedOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO createPurchaseOrderFromCart(String sessionId, Authentication authentication) {
        logger.info("Creating purchase order from cart, sessionId: {}", sessionId);
        validateAuthentication(authentication, "ROLE_CLIENT");
        sessionId = validateSessionId(sessionId);

        Long clientId = Long.valueOf(authentication.getName());
        Client client = clientRepository.findById(clientId)
                .filter(Client::getActive)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));
        Cart cart = cartService.getOrCreateCart(sessionId, authentication).toEntity();
        if (cart.getItems().isEmpty()) {
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new IllegalStateException("Cart is empty");
        }

        if (cart.getClient() != null && !cart.getClient().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to use cart: {}", clientId, cart.getId());
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new SecurityException("Not authorized to use this cart");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setTicket(UUID.randomUUID().toString());
        order.setAmount(calculateTotalAmount(cart));
        order.setPaymentMethod(PaymentMethod.CREDIT); // Default
        order.setAddress(client.getAddresses().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Client has no addresses")));
        order.setClient(client);
        order.setCustomerRut(client.getCustomerRut());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setActive(true);

        Set<Details> details = cart.getItems().stream().map(item -> {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                podStreamPrometheusConfig.incrementOrderErrors();
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
            Details detail = new Details();
            detail.setPurchaseOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(product.getPrice());
            return detail;
        }).collect(Collectors.toSet());
        order.setDetails(details);

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        historyService.createHistory(savedOrder.getId(), null, OrderStatus.PENDING_PAYMENT, authentication);
        searchRepository.save(savedOrder);
        redisTemplate.opsForValue().set(ORDER_KEY_PREFIX + savedOrder.getId(), savedOrder, ORDER_TTL_MINUTES, TimeUnit.MINUTES);
        cartService.clearCart(sessionId, authentication);

        sendInvoiceEmail(savedOrder);
        podStreamPrometheusConfig.incrementOrderCreated();
        return new PurchaseOrderDTO(savedOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus, String changedBy, Authentication authentication) {
        logger.info("Updating order status, orderId: {}, newStatus: {}", orderId, newStatus);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .filter(PurchaseOrder::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getClient().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to update order: {}", clientId, orderId);
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new SecurityException("Not authorized to update this order");
        }

        order.setStatus(newStatus);
        PurchaseOrder updatedOrder = purchaseOrderRepository.save(order);
        historyService.createHistory(updatedOrder.getId(), null, newStatus, authentication);
        searchRepository.save(updatedOrder);
        redisTemplate.opsForValue().set(ORDER_KEY_PREFIX + updatedOrder.getId(), updatedOrder, ORDER_TTL_MINUTES, TimeUnit.MINUTES);

        sendStatusUpdateEmail(updatedOrder);
        podStreamPrometheusConfig.incrementOrderStatusUpdated();
        return new PurchaseOrderDTO(updatedOrder);
    }

    @Override
    public PurchaseOrderDTO getOrder(Long orderId, Authentication authentication) {
        logger.info("Fetching order, orderId: {}", orderId);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        String cacheKey = ORDER_KEY_PREFIX + orderId;
        PurchaseOrder cachedOrder = (PurchaseOrder) redisTemplate.opsForValue().get(cacheKey);
        if (cachedOrder != null && cachedOrder.isActive()) {
            logger.info("Order id: {} retrieved from cache", orderId);
            podStreamPrometheusConfig.incrementOrderCacheHit();
            return new PurchaseOrderDTO(cachedOrder);
        }

        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .filter(PurchaseOrder::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getClient().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to view order: {}", clientId, orderId);
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new SecurityException("Not authorized to view this order");
        }

        redisTemplate.opsForValue().set(cacheKey, order, ORDER_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementOrderFetched();
        return new PurchaseOrderDTO(order);
    }

    @Override
    public PurchaseOrderDTO getOrderByTicket(String ticket, Authentication authentication) {
        logger.info("Fetching order by ticket: {}", ticket);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        String cacheKey = ORDER_KEY_PREFIX + "ticket:" + ticket;
        PurchaseOrder cachedOrder = (PurchaseOrder) redisTemplate.opsForValue().get(cacheKey);
        if (cachedOrder != null && cachedOrder.isActive()) {
            logger.info("Order ticket: {} retrieved from cache", ticket);
            podStreamPrometheusConfig.incrementOrderCacheHit();
            return new PurchaseOrderDTO(cachedOrder);
        }

        PurchaseOrder order = purchaseOrderRepository.findByTicketAndActiveTrue(ticket)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ticket: " + ticket));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getClient().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to view order with ticket: {}", clientId, ticket);
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new SecurityException("Not authorized to view this order");
        }

        redisTemplate.opsForValue().set(cacheKey, order, ORDER_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(ORDER_KEY_PREFIX + order.getId(), order, ORDER_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementOrderFetched();
        return new PurchaseOrderDTO(order);
    }

    @Override
    public List<PurchaseOrderDTO> getOrdersByClient(Authentication authentication) {
        logger.info("Fetching orders for client");
        Long clientId = validateAuthentication(authentication, "ROLE_CLIENT");

        List<PurchaseOrder> orders = purchaseOrderRepository.findByClientIdAndActiveTrue(clientId);
        podStreamPrometheusConfig.incrementOrderFetched();
        return orders.stream()
                .map(order -> {
                    redisTemplate.opsForValue().set(ORDER_KEY_PREFIX + order.getId(), order, ORDER_TTL_MINUTES, TimeUnit.MINUTES);
                    redisTemplate.opsForValue().set(ORDER_KEY_PREFIX + "ticket:" + order.getTicket(), order, ORDER_TTL_MINUTES, TimeUnit.MINUTES);
                    return new PurchaseOrderDTO(order);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Authentication authentication) {
        logger.info("Deleting order, orderId: {}", orderId);
        validateAuthentication(authentication, "ROLE_CLIENT", "ROLE_ADMIN");

        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .filter(PurchaseOrder::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        Long clientId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getClient().getId().equals(clientId)) {
            logger.warn("Client id: {} not authorized to delete order: {}", clientId, orderId);
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new SecurityException("Not authorized to delete this order");
        }

        order.setActive(false);
        purchaseOrderRepository.save(order);
        searchRepository.save(order);
        redisTemplate.delete(ORDER_KEY_PREFIX + orderId);
        redisTemplate.delete(ORDER_KEY_PREFIX + "ticket:" + order.getTicket());
        podStreamPrometheusConfig.incrementOrderDeleted();
    }

    private void sendInvoiceEmail(PurchaseOrder order) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String email = order.getClient().getEmail();
            validateEmail(email, order.getId());
            helper.setTo(email);
            helper.setSubject("Factura #" + order.getTicket());
            helper.setText("Adjuntamos tu factura electrónica.");
            helper.addAttachment("factura.pdf", new ByteArrayResource(pdfService.generateInvoice(order)));
            mailSender.send(message);
            podStreamPrometheusConfig.incrementEmailSent();
        } catch (MessagingException e) {
            logger.error("Error sending invoice for order #{}", order.getId(), e);
            podStreamPrometheusConfig.incrementEmailErrors();
            throw new RuntimeException("Error sending invoice email", e);
        }
    }

    private void sendStatusUpdateEmail(PurchaseOrder order) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String email = order.getClient().getEmail();
            validateEmail(email, order.getId());
            helper.setTo(email);
            helper.setSubject("Actualización de Orden #" + order.getTicket());
            helper.setText("Tu orden ha sido actualizada.\nEstado: " + order.getStatus().getDescription());
            mailSender.send(message);
            podStreamPrometheusConfig.incrementEmailSent();
        } catch (MessagingException e) {
            logger.error("Error sending status update for order #{}", order.getId(), e);
            podStreamPrometheusConfig.incrementEmailErrors();
            throw new RuntimeException("Error sending status update email", e);
        }
    }

    private double calculateTotalAmount(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        return total * (1 - cart.getDiscount() / 100);
    }

    private Long validateAuthentication(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new SecurityException("Authentication required");
        }
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> List.of(roles).contains(auth.getAuthority()));
        if (!hasRole) {
            logger.warn("User {} does not have required roles: {}", authentication.getName(), String.join(", ", roles));
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new SecurityException("Insufficient permissions");
        }
        return Long.valueOf(authentication.getName());
    }

    private String validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    private void validateEmail(String email, Long orderId) {
        if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            logger.warn("Invalid or empty email for order #{}", orderId);
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new IllegalArgumentException("Invalid or empty client email");
        }
    }

    private void validateOrderDTO(PurchaseOrderDTO orderDTO) {
        if (orderDTO.getDetails().isEmpty()) {
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new IllegalArgumentException("Order must have at least one detail");
        }
        if (orderDTO.getAmount() <= 0) {
            podStreamPrometheusConfig.incrementOrderErrors();
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }
}