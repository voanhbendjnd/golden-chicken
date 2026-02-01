package vn.edu.fpt.golden_chicken.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import vn.edu.fpt.golden_chicken.repositories.UserRepository;

@Configuration
public class PermissionInterceptorConfig implements WebMvcConfigurer {
    private final UserRepository userRepository;

    public PermissionInterceptorConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor(userRepository);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/",
                "/home",
                "/login",
                "/register",
                "/css/**",
                "/js/**",
                "/images/**",
                "/img/**",
                "/icon/**",
                "/client/**",
                "/access-deny",
                "/favicon.ico"
        };
        registry.addInterceptor(getPermissionInterceptor())
                .addPathPatterns("/admin/**", "/staff/**")
                .excludePathPatterns(whiteList);
    }
}
