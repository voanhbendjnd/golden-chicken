package vn.edu.fpt.golden_chicken.services.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.domain.response.OrderMessage;
import vn.edu.fpt.golden_chicken.domain.response.ReviewMessage;
import vn.edu.fpt.golden_chicken.domain.response.UserMessage;
import vn.edu.fpt.golden_chicken.domain.response.VerifyAccountMessage;
import vn.edu.fpt.golden_chicken.services.MailService;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailConsumer {
    MailService mailService;
    StringRedisTemplate redis;

    @KafkaListener(topics = "order-chicken-topic", groupId = "email-group")
    public void listenOrderAndSendMail(OrderMessage msg) {
        System.out.println(">>>> KAFKA ACCEPT REQUEST ORDER");

        if (msg.getStatus() == OrderStatus.SHIPPER_ISSUE || msg.getStatus() == OrderStatus.DELIVERY_FAILED) {
            this.mailService.sendStatus(
                    msg.getCustomerEmail(),
                    "Delivery Problem Update - Order #" + msg.getOrderId(),
                    "mail/os",
                    msg.getCustomerEmail(),
                    msg.getStatus().name(),
                    String.valueOf(msg.getOrderId()),
                    msg.getCustomerName(),
                    msg.getReason());
        } else {
            this.mailService.sendStatus(
                    msg.getCustomerEmail(),
                    "Confirm Order With ID #" + msg.getOrderId(),
                    "mail/os",
                    msg.getCustomerEmail(),
                    msg.getStatus().name(),
                    String.valueOf(msg.getOrderId()),
                    msg.getCustomerName());
        }

        System.out.println(">>> SEND MAIL SUCCESS");

    }

    @KafkaListener(topics = "customer-account-topic", groupId = "email-group")
    public void listenOrderAndSendMailVerify(VerifyAccountMessage msg) {
        System.out.println(">>>> KAFKA ACCEPT REQUEST ORDER");
        var otp = this.redis.opsForValue().get(DeclareConstant.USER_OTP + msg.getEmail());
        this.mailService.sendOTP(msg.getEmail(), "Verify Account", "mail/otp", msg.getEmail(), otp);
        System.out.println(">>> SEND MAIL SUCCESS");

    }

    @KafkaListener(topics = "forgot-password-account-topic", groupId = "email-group")
    public void listenOrderAndSendMailVerifyForgotPassword(VerifyAccountMessage msg) {
        System.out.println(">>>> KAFKA ACCEPT REQUEST ORDER");
        var otp = this.redis.opsForValue().get("FORGOT_PASSWORD:" + msg.getEmail());
        this.mailService.sendOTP(msg.getEmail(), "Verify Account", "mail/otp", msg.getEmail(), otp);
        System.out.println(">>> SEND MAIL SUCCESS");

    }

    @KafkaListener(topics = "security-account-topic", groupId = "email-group")
    public void listenLoginFailuresEvent(String email) {
        this.mailService.sendSecurityLoginFailures(email, "Lock Account About 3 Minutes", "mail/ban", email);
        System.out.println("SEND MAIL BAN SUCCESS!");
    }

    @KafkaListener(topics = "violate-review-topic", groupId = "email-group")
    public void listViolateReviewCustomer(ReviewMessage message) {

        this.mailService.sendMailViolateWordAfterReview(message.getEmail(), "Language used to describe violations",
                "mail/violate.review", message.getEmail(), message.getProductName(),
                String.valueOf(message.getRecord()));
    }

    @KafkaListener(topics = "violate-account-topic", groupId = "email-group")
    public void listenBanViolate(String email) {
        this.mailService.sendSecurityLoginFailures(email, "Lock Account About 1 Week!", "mail/ban.violate", email);
        System.out.println("SEND MAIL BAN SUCCESS!");
    }

    @KafkaListener(topics = "create-account-topic", groupId = "email-group")
    public void listenCreateAccount(UserMessage msg) {
        this.mailService.sendEmailAndPasswordForStaff(msg.getEmail(), "Create account success!", "mail/sas",
                msg.getEmail(), msg.getName(), msg.getPassword());
        System.out.println("SEND MAIL CREATE SUCCESS!");

    }

    @KafkaListener(topics = "state-account-topic", groupId = "email-group")
    public void allowUserStateAccount(String email) {
        this.mailService.recoverPasswordSuccess(email, "Recover Password", "mail/recover.password", email);
        System.out.println("SEND MAIL RECOVER UPDATE SUCCESS!");

    }
}
