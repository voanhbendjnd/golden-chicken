package vn.edu.fpt.golden_chicken.services.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import vn.edu.fpt.golden_chicken.domain.response.OrderMessage;
import vn.edu.fpt.golden_chicken.domain.response.VerifyAccountMessage;
import vn.edu.fpt.golden_chicken.services.MailService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailConsumer {
    MailService mailService;

    @KafkaListener(topics = "order-chicken-topic", groupId = "email-group")
    public void listenOrderAndSendMail(OrderMessage msg) {
        System.out.println(">>>> KAFKA ACCEPT REQUEST ORDER");

        this.mailService.sendStatus(msg.getCustomerEmail(), "Confirm Order With ID #" + msg.getOrderId(), "mail/os",
                msg.getCustomerEmail(), msg.getStatus().name(), String.valueOf(msg.getOrderId()),
                msg.getCustomerName());

        System.out.println(">>> SEND MAIL SUCCESS");

    }

    @KafkaListener(topics = "customer-account-topic", groupId = "email-group")
    public void listenOrderAndSendMailVerify(VerifyAccountMessage msg) {
        System.out.println(">>>> KAFKA ACCEPT REQUEST ORDER");
        this.mailService.sendOTP(msg.getEmail(), "Verify Account", "mail/otp", msg.getEmail(), msg.getOtp());
        System.out.println(">>> SEND MAIL SUCCESS");

    }
}
