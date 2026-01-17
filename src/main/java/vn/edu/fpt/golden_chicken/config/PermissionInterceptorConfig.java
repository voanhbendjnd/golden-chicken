package vn.edu.fpt.golden_chicken.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import vn.edu.fpt.golden_chicken.repositories.UserRepository;

@Configuration
public class PermissionInterceptorConfig {
    private final UserRepository userRepository;

    public PermissionInterceptorConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor(userRepository);
    }

    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/login",
                "/register",
                "/"
        };
        registry.addInterceptor(getPermissionInterceptor()).excludePathPatterns(whiteList);
    }
}
