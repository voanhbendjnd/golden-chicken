package vn.edu.fpt.golden_chicken.services;

import java.util.Collections;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;

import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.redis.RedisUserService;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RedisUserService redisUserService;

    public CustomUserDetailsService(UserRepository userRepository, RedisUserService redisUserService) {
        this.userRepository = userRepository;
        this.redisUserService = redisUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        vn.edu.fpt.golden_chicken.domain.entity.User user = this.userRepository
                .findByEmailIgnoreCaseAndStatus(username.toLowerCase(), true);
        if (this.redisUserService.isAccountLocked(username)) {
            throw new LockedException("Account has locked!");
        }
        if (this.redisUserService.isAccountLockedWhenReview(username)) {
            throw new LockedException("Account has locked when review!");

        }
        if (user == null) {
            throw new UsernameNotFoundException("Not Found User With email: " + username);
        }
        return new User(
                user.getEmail().toLowerCase(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
    }
}