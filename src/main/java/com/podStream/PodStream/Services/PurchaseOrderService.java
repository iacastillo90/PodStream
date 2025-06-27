package com.podStream.PodStream.Services;

import com.podStream.PodStream.Models.*;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.PurchaseOrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseOrderService {

    @Autowired
    private PDFService pdfService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private CartService cartService;

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);

    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        // Obtener el usuario autenticado desde el contexto de seguridad
        Client authenticatedUser = (Client) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser == null) {
            throw new RuntimeException("No se encontró un usuario autenticado");
        }
        order.setClient(authenticatedUser);

        // Establecer estado inicial y registrar en el historial
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        OrderStatusHistory initialHistory = new OrderStatusHistory();
        initialHistory.setPurchaseOrder(order);
        initialHistory.setNewStatus(OrderStatus.PENDING_PAYMENT);
        initialHistory.setChangedBy("system");
        order.getStatusHistory().add(initialHistory);

        // Guardar la orden
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        logger.info("Orden de compra creada: ID={}, Estado={}", savedOrder.getId(), savedOrder.getStatus());

        // Generar y enviar factura
        byte[] pdfBytes = pdfService.generateInvoice(savedOrder);
        sendInvoiceEmail(savedOrder, pdfBytes);

        // Registrar métrica
        meterRegistry.counter("orders.created", "status", savedOrder.getStatus().toString()).increment();

        return savedOrder;
    }

    @Transactional
    public PurchaseOrder createPurchaseOrderFromCart (String sessionId) {
        Client authenticatedUser = (Client) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser == null){
            throw new RuntimeException("No se encontró un usuario autenticado");
        }

        Cart cart = cartService.getOrCreateCart(sessionId);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vací́o");
        }

        // Crear PurchaseOrder desde Cart
        PurchaseOrder order = new PurchaseOrder();
        order.setClient(authenticatedUser);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setAmount(calculateTotalAmount(cart));

        // Transferir items del carrito a la orden
        Set<Details> orderItems = new HashSet<>();
        for (CartItem item : cart.getItems()) {
            Details details = new Details();
            details.setPurchaseOrder(order);
            details.setProduct(item.getProduct());
            details.setQuantity(item.getQuantity());
            details.setPrice(item.getProduct().getPrice());
            orderItems.add(details);
        }
        order.setDetails(orderItems);

        // Establecer estado inicial
        OrderStatusHistory initialHistory = new OrderStatusHistory();
        initialHistory.setPurchaseOrder(order);
        initialHistory.setNewStatus(OrderStatus.PENDING_PAYMENT);
        initialHistory.setChangedBy("system");
        order.getStatusHistory().add(initialHistory);

        // Guardar la orden
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        logger.info("Orden de compra creada: ID={}, Estado={}", savedOrder.getId(), savedOrder.getStatus());

        //Vaciar el carrito
        cartService.clearCart(sessionId);


        // Generar y enviar factura
        byte[] pdfBytes = pdfService.generateInvoice(savedOrder);
        sendInvoiceEmail(savedOrder, pdfBytes);

        // Registrar métrica
        meterRegistry.counter("orders.created", "status", savedOrder.getStatus().toString()).increment();

        return savedOrder;

    }

    private double calculateTotalAmount(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public PurchaseOrder updateOrderStatus(Long orderId, OrderStatus newStatus, String changedBy) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));

        // Registrar cambio de estado
        OrderStatusHistory history = new OrderStatusHistory();
        history.setPurchaseOrder(order);
        history.setOldStatus(order.getStatus());
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        order.getStatusHistory().add(history);

        // Actualizar estado
        order.setStatus(newStatus);
        PurchaseOrder updatedOrder = purchaseOrderRepository.save(order);
        logger.info("Orden de compra actualizada: ID={}, Nuevo estado={}", updatedOrder.getId(), newStatus);

        // Registrar métrica
        meterRegistry.counter("orders.status.updated", "new_status", newStatus.toString()).increment();

        // Enviar notificación al cliente
        sendStatusUpdateEmail(updatedOrder);

        return updatedOrder;
    }

    private void sendInvoiceEmail(PurchaseOrder order, byte[] pdfBytes) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String email = order.getClient().getEmail();
            if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                logger.warn("El correo del cliente es inválido o está vacío para la orden #{}", order.getId());
                throw new IllegalArgumentException("El correo del cliente es inválido o está vacío");
            }
            helper.setTo(email);
            helper.setSubject("Factura #" + order.getTicket());
            helper.setText("Adjuntamos tu factura electrónica.");
            helper.addAttachment("factura.pdf", new ByteArrayResource(pdfBytes));
            mailSender.send(message);
            meterRegistry.counter("emails.sent", "type", "invoice").increment();
        } catch (MessagingException e) {
            logger.error("Error al enviar factura para la orden #{}", order.getId(), e);
            meterRegistry.counter("emails.errors", "type", "invoice").increment();
            throw new RuntimeException("Error al enviar la factura por correo", e);
        }
    }

    private void sendStatusUpdateEmail(PurchaseOrder order) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String email = order.getClient().getEmail();
            if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                logger.warn("El correo del cliente es inválido o está vacío para la orden #{}", order.getId());
                throw new IllegalArgumentException("El correo del cliente es inválido o está vacío");
            }
            helper.setTo(email);
            helper.setSubject("Actualización de Orden #" + order.getTicket());
            helper.setText("Tu orden ha sido actualizada.\nEstado: " + order.getStatus().getDescription());
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Error al enviar actualización de estado para la orden #{}", order.getId(), e);
            throw new RuntimeException("Error al enviar correo de actualización", e);
        }
    }
}