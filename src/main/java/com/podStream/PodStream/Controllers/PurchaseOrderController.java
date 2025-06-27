package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.Models.Cart;
import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.PurchaseOrder;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Services.CartService;
import com.podStream.PodStream.Services.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private CartService cartService;

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);

    @PostMapping("/create")
    @Operation(summary = "Crear una nueva orden de compra")
    public ResponseEntity<PurchaseOrder> createOrder(@RequestBody PurchaseOrder order) {
        logger.info("Creando una nueva orden de compra: {}", order);
        try {
            logger.info("Nueva orden de compra creada: {}", order);
            return ResponseEntity.ok(purchaseOrderService.createPurchaseOrder(order));
        } catch (Exception e) {
            logger.error("Error al crear la orden de compra: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Actualizar el estado de una orden")
    public ResponseEntity<PurchaseOrder> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @RequestParam String changedBy) {
        return ResponseEntity.ok(purchaseOrderService.updateOrderStatus(id, status, changedBy));
    }

    @PostMapping("/create-from-cart")
    @Operation(summary = "Crear una nueva orden de compra a partir del carrito")
    public ResponseEntity<PurchaseOrder> createOrderFromCart(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                                             @AuthenticationPrincipal Client client) {

        if (client == null) {
            logger.warn("Intento de crear orden sin autenticación");
            return ResponseEntity.status(401).body(null); // Requiere autenticación
        }

        if (sessionId == null || sessionId.isEmpty()) {
            logger.warn("No se proporcionó sessionId en X-Session-Id");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Cart cart = cartService.getOrCreateCart(sessionId);
            if (cart.getItems().isEmpty()) {
                logger.warn("El carrito está vacío para sessionId: {}", sessionId);
                return ResponseEntity.badRequest().body(null);
            }

            PurchaseOrder createdOrder = purchaseOrderService.createPurchaseOrderFromCart(sessionId);
            logger.info("Orden creada desde el carrito para el usuario {}: {}", client.getId(), createdOrder);
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            logger.error("Error al crear la orden desde el carrito: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
