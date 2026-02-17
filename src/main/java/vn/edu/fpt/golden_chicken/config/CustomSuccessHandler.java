package vn.edu.fpt.golden_chicken.config;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.fpt.golden_chicken.services.UserService;

public class CustomSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserService userService;

    protected String determineTargetUrl(final Authentication authentication) {

        var roleTargetUrlMap = new HashMap<String, String>();
        roleTargetUrlMap.put("ROLE_ADMIN", "/admin");
        roleTargetUrlMap.put("ROLE_STAFF", "/staff");
        roleTargetUrlMap.put("ROLE_CUSTOMER", "/");
        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (final GrantedAuthority grantedAuthority : authorities) {
            String authorityName = grantedAuthority.getAuthority();
            if (roleTargetUrlMap.containsKey(authorityName)) {
                return roleTargetUrlMap.get(authorityName);
            }
        }

        throw new IllegalStateException();
    }

    /*
     * Thay vì gọi DB lấy Name thì hàm này giúp chỉ cần làm 1 lần duy nhất
     */
    protected void clearAuthenticationAttributes(HttpServletRequest request, Authentication authentication) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        // dọn các thông báo lỗi trước đó
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        String email = authentication.getName();
        var user = this.userService.getByEmail(email);
        if (user != null) {
            session.setAttribute("id", user.getId());
            session.setAttribute("user", user);
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("roleName", user.getRole().getName());
            if (user.getRole().getName().equals("CUSTOMER")) {
                Integer sum = user.getCustomer().getCartItems().size();
                session.setAttribute("cartCount", sum != null ? sum : 0);
            }

            // int sum = user.getCart() == null ? 0 : user.getCart().getSum();
            // session.setAttribute("sum", sum);
        }

    }

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {

            return;
        }
        clearAuthenticationAttributes(request, authentication);

        redirectStrategy.sendRedirect(request, response, targetUrl);

    }

}
