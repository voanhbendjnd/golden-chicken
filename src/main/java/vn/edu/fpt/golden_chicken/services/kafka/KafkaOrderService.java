package vn.edu.fpt.golden_chicken.services.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.response.OrderMessage;

@Service
public class KafkaOrderService {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderUpdate(Order order) {
        OrderMessage msg = new OrderMessage(
                order.getId(),
                order.getCustomer().getUser().getEmail(),
                order.getName(),
                order.getStatus(),
                order.getFinalAmount(), "Not thing!");

        this.kafkaTemplate.send("order-chicken", msg);
    }
}
