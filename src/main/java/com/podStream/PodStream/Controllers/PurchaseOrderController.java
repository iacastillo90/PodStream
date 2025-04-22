package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.Models.OrderStatus;
import com.podStream.PodStream.Models.PurchaseOrder;
import com.podStream.PodStream.Services.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderService purchaseOrderService;

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
}
