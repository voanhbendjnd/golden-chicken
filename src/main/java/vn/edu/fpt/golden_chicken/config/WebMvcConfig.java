package vn.edu.fpt.golden_chicken.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final PermissionInterceptor permissionInterceptor;

    @Value("${djnd.upload-file.base-uri}")
    private String baseURI;

    public WebMvcConfig(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");
        registry.addResourceHandler("/icon/**").addResourceLocations("classpath:/static/icon/");
        registry.addResourceHandler("/client/**").addResourceLocations("classpath:/static/client/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + baseURI + "/");
    }

    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    // registry.addInterceptor(permissionInterceptor)
    // .addPathPatterns("/admin/**", "/staff/**")
    // .excludePathPatterns("/login", "/register", "/css/**", "/js/**",
    // "/images/**");
    // }
}
