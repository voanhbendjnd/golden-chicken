package vn.edu.fpt.golden_chicken.services.kafka;

import java.math.BigDecimal;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import vn.edu.fpt.golden_chicken.domain.response.OrderMessage;

@Service
public class RevenueService {
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @KafkaListener(topics = "order-chicken-topic", groupId = "revenue-report-group-new", properties = {
            "auto.offset.reset=earliest" })
    public void calculateRevenue(OrderMessage msg) {
        totalRevenue = totalRevenue.add(msg.getTotalPrice());
        System.out.println(">>>> Add success: " + msg.getTotalPrice() + ". Total new: " + totalRevenue);
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
