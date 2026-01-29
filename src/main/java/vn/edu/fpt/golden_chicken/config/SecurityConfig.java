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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;

import jakarta.servlet.DispatcherType;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.CustomUserDetailsService;

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
        public UserDetailsService userDetailsService(UserRepository userRepository) {
                return new CustomUserDetailsService(userRepository);
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
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/client/**",
                };
                http
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll()

                                )
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
                                                // luôn tạo session giữ cho trang thái đăng nhập
                                                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                                                .invalidSessionUrl("/logout?expired")
                                                // chỉ 1 browser được đăng nhập
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false))
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
