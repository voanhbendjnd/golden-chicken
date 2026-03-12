package vn.edu.fpt.golden_chicken.services.Auth;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.services.redis.RedisUserService;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationEvents {

    RedisUserService redisUserService;

    KafkaTemplate<String, String> securityMail;

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        var count = this.redisUserService.getNumberOfLoginFailures(username) != null
                ? this.redisUserService.getNumberOfLoginFailures(username)
                : 0;
        if (count >= 5) {
            this.redisUserService.lockAccount(username);
            System.out.println("Account with email " + username + " status locked!");
            this.securityMail.send("security-account-topic", username);
            return;
        }
        this.redisUserService.saveNumberOfLoginFailures(username);

        System.out.println("Login account failures: " + username);
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        this.redisUserService.resetLoginFailures(username);
    }
}