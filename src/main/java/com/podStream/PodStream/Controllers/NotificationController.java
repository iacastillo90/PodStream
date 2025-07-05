package com.podStream.PodStream.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyStockUpdate(Long productId, int newStock) {
        messagingTemplate.convertAndSend("/topic/stock", "Product " + productId + " stock updated to " + newStock);
    }
}
