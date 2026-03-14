package vn.edu.fpt.golden_chicken.controllers.staff;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.response.LocationMessage;
import vn.edu.fpt.golden_chicken.services.ShipperLocationStore;

@Controller
public class LocationController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ShipperLocationStore shipperLocationStore;

    public LocationController(SimpMessagingTemplate messagingTemplate, ShipperLocationStore shipperLocationStore) {
        this.messagingTemplate = messagingTemplate;
        this.shipperLocationStore = shipperLocationStore;
    }

    /**
     * Shipper gửi vị trí GPS qua STOMP → broadcast cho customer theo dõi.
     * Destination: /app/location.update
     * Broadcast to: /topic/location/{orderId}
     */
    @MessageMapping("/location.update")
    public void updateLocation(@Payload LocationMessage msg) {
        if (msg.getOrderId() == null) return;
        shipperLocationStore.save(msg);
        messagingTemplate.convertAndSend("/topic/location/" + msg.getOrderId(), msg);
    }
}
