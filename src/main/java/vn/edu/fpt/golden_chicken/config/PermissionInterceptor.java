package vn.edu.fpt.golden_chicken.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Transactional
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;

    public PermissionInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object hanlder)
            throws Exception {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            var email = authentication.getName();
            String apiPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            var httpMethod = request.getMethod();
            var user = this.userRepository.findByEmail(email);
            if (user != null) {
                var role = user.getRole();
                if (role != null) {
                    var permissions = role.getPermissions();
                    var isAllow = permissions.stream()
                            .anyMatch(p -> p.getApiPath().equals(apiPath) && p.getMethod().endsWith(httpMethod));
                    if (!isAllow) {
                        throw new PermissionException("You do not have permission!");
                    }
                } else {
                    throw new PermissionException("You do not have permission!");

                }
            }
            return true;
        }
        return false;

    }
}
