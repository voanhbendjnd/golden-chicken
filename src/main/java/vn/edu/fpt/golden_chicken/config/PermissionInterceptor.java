package vn.edu.fpt.golden_chicken.config;

import java.util.Arrays;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Transactional
public class PermissionInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;
    AntPathMatcher pathMatcher = new AntPathMatcher();

    public PermissionInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object hanlder)
            throws Exception {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            return true;
        }

        var email = authentication.getName();
        String apiPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        var httpMethod = request.getMethod();
        var user = this.userRepository.findByEmailWithRolePermissions(email);

        if (user != null) {
            var role = user.getRole();

            if (role != null) {
                if ("STAFF".equalsIgnoreCase(role.getName())) {
                    var staffType = user.getStaff() != null ? user.getStaff().getStaffType() : null;
                    if (!isStaffPathAllowed(staffType, apiPath)) {
                        throw new PermissionException("You do not have permission!");
                    }
                }

                var permissions = role.getPermissions();
                var isAllow = permissions.stream()
                        .anyMatch(p -> pathMatcher.match(p.getApiPath(), apiPath)
                                && Arrays.stream(p.getMethod().split(","))
                                        .map(String::trim)
                                        .anyMatch(m -> m.equalsIgnoreCase(httpMethod)));
                if (!isAllow) {
                    throw new PermissionException("You do not have permission!");
                }
            } else {
                throw new PermissionException("You do not have permission!");
            }
        }
        return true;
    }

    private boolean isStaffPathAllowed(StaffType staffType, String apiPath) {
        if (staffType == null || apiPath == null) {
            return false;
        }

        if (pathMatcher.match("/staff", apiPath)) {
            return true;
        }

        return switch (staffType) {
            case MANAGER -> true;
            case SHIPPER -> pathMatcher.match("/staff/order/**", apiPath);
            case RECEPTIONIST -> pathMatcher.match("/staff/order/**", apiPath)
                    || pathMatcher.match("/staff/voucher/**", apiPath)
                    || pathMatcher.match("/staff/product/**", apiPath)
                    || pathMatcher.match("/staff/category/**", apiPath)
                    || pathMatcher.match("/staff/combo/**", apiPath);
        };
    }
}
