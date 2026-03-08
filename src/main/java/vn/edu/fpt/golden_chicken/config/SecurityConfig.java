package vn.edu.fpt.golden_chicken.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
// import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;

import jakarta.servlet.DispatcherType;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.CustomUserDetailsService;
import vn.edu.fpt.golden_chicken.services.redis.RedisUserService;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@EnableWebSecurity
public class SecurityConfig {

    // @Bean
    // public RoleHierarchy roleHierarchy() {
    // RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    // // Admin có mọi quyền của Staff, Staff có mọi quyền của Customer
    // roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_STAFF \n ROLE_STAFF >
    // ROLE_CUSTOMER");
    // return roleHierarchy;
    // }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Vị trí lấy thông tin User
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository, RedisUserService redisUserService) {
        return new CustomUserDetailsService(userRepository, redisUserService);
    }

    /*
     * So sánh passowrd truyền về
     */
    @Bean
    public DaoAuthenticationProvider authProvider(PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        // authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /*
     * Tự động trả về trang khớp với role người đó
     */
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new CustomSuccessHandler();

    }

    @Bean
    public SpringSessionRememberMeServices rememberMeServices() {
        SpringSessionRememberMeServices rememberMeServices = new SpringSessionRememberMeServices();

        // optionally customize
        rememberMeServices.setAlwaysRemember(true);
        return rememberMeServices;
    }

    // override form login
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String[] whiteList = {
                "/",
                "/home",
                "/login",
                "/register",
                "/forgot-password",
                "/forgot-password/**",
                "/verify-otp",
                "/reset-password",
                "/css/**",
                "/js/**",
                "/images/**",
                "/img/**",
                "/client/**",
                "/favicon.ico",
                "/fonts/**",
                "/menu/**",
                "/payment/**",
                "/verify/**"

        };
        http
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        // chuyển sang trang jsp không bị chặn
                        .dispatcherTypeMatchers(DispatcherType.FORWARD,
                                DispatcherType.INCLUDE)
                        .permitAll()
                        // .requestMatchers(HttpMethod.GET, "/product/**").permitAll()
                        // .requestMatchers(HttpMethod.POST, "/product/**")
                        // .hasAnyRole("ADMIN", "STAFF")
                        // .requestMatchers("/admin/**").hasRole("ADMIN")
                        // .requestMatchers("/staff/**").hasRole("STAFF")
                        // còn lại thì phải login mới vô được
                        .anyRequest().authenticated())
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        // Sửa dòng này: Cho phép Spring quản lý việc đổi Session ID linh hoạt
                        // hơn
                        .sessionFixation().migrateSession()
                        .invalidSessionUrl("/login?invalid")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry()))
                .logout(logout -> logout.deleteCookies("JSESSIONID").invalidateHttpSession(true))
                .rememberMe(r -> r.rememberMeServices(rememberMeServices()))
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .failureUrl("/login?error")
                        .successHandler(customSuccessHandler())
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-deny"));
        return http.build();
    }

    // end security
}
