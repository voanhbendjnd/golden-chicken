package vn.edu.fpt.golden_chicken.services.Auth;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import vn.edu.fpt.golden_chicken.services.redis.RedisUserService;

@Component
public class AuthenticationEvents {

    private final RedisUserService redisUserService;

    public AuthenticationEvents(RedisUserService redisUserService) {
        this.redisUserService = redisUserService;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();

        this.redisUserService.saveNumberOfLoginFailures(username);

        System.out.println("Login thất bại cho user: " + username);
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        this.redisUserService.resetLoginFailures(username);
    }
}