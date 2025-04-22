package com.podStream.PodStream.Services;

import com.podStream.PodStream.Models.*;
import com.podStream.PodStream.Models.User.User;
import com.podStream.PodStream.Repositories.PurchaseOrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PurchaseOrderServiceTest {

    @Mock
    private PDFService pdfService;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private MimeMessage mimeMessage;
    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(meterRegistry.counter(any(), any(), any())).thenReturn(new SimpleMeterRegistry().counter("test"));
    }

    @Test
    void createPurchaseOrder_success() {
        // Arrange
        PurchaseOrder order = new PurchaseOrder(
                "TICKET123",
                1000.0,
                LocalDateTime.now(),
                PaymentMethod.CREDIT,
                new Address(),
                new User(),
                    "12.345.678-9"
        );
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(order);
        when(pdfService.generateInvoice(any(PurchaseOrder.class))).thenReturn(new byte[]{});

        // Act
        PurchaseOrder result = purchaseOrderService.createPurchaseOrder(order);

        // Assert
        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
        assertEquals(1, result.getStatusHistory().size());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void updateOrderStatus_success() {
        // Arrange
        PurchaseOrder order = new PurchaseOrder(
                "TICKET123",
                1000.0,
                LocalDateTime.now(),
                PaymentMethod.CREDIT,
                new Address(),
                new User(),
                "12.345.678-9"
        );
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        when(purchaseOrderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(order);

        // Act
        PurchaseOrder result = purchaseOrderService.updateOrderStatus(1L, OrderStatus.SHIPPED, "system");

        // Assert
        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        assertEquals(1, result.getStatusHistory().size());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}