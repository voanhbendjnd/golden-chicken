package vn.edu.fpt.golden_chicken.services.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import vn.edu.fpt.golden_chicken.domain.response.OrderMessage;

@Service
public class MailConsumer {
    @Autowired
    private JavaMailSender mailSender;

    @KafkaListener(topics = "order-chicken-topic", groupId = "email-group")
    public void listenOrderAndSendMail(OrderMessage msg) {
        System.out.println(">>>> Kafka ACCEPT REQUEST");
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(msg.getCustomerEmail());
        email.setSubject("Confirm Order #" + msg.getOrderId());
        email.setText("Order Status: " + msg.getStatus());
        this.mailSender.send(email);
        System.out.println(">>> SEND MAIL SUCCESS");

    }
}
