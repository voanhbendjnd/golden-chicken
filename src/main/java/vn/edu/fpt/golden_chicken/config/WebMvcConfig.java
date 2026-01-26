package vn.edu.fpt.golden_chicken.config;

import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    public WebMvcConfig(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/client/**").addResourceLocations("classpath:/static/client/");
    }

    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    // registry.addInterceptor(permissionInterceptor)
    // .addPathPatterns("/admin/**", "/staff/**")
    // .excludePathPatterns("/login", "/register", "/css/**", "/js/**",
    // "/images/**");
    // }
}