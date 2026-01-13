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
public class CustomUserDetailsService implements UserDetailsService { // Sửa tên class cho chuẩn
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username ở đây chính là Email từ form login gửi lên
        vn.edu.fpt.golden_chicken.domain.entity.User user = this.userRepository.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("Not Found User With email: " + username);
        }

        // Trả về đối tượng User của Spring Security
        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
    }
}