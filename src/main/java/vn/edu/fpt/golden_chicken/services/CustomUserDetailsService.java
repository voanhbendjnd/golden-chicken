package vn.edu.fpt.golden_chicken.services;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;

import vn.edu.fpt.golden_chicken.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        vn.edu.fpt.golden_chicken.domain.entity.User user = this.userRepository.findByEmail(username.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException("Not Found User With email: " + username);
        }

        return new User(
                user.getEmail().toLowerCase(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
    }
}