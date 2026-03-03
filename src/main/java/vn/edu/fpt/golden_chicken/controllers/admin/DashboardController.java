package vn.edu.fpt.golden_chicken.controllers.admin;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DashboardController {
    UserService userService;
    OrderRepository orderRepository;
    OrderService orderService;

    @GetMapping("/admin")
    public String getDashboardAdminPage() {
        return "admin/dashboard";
    }

    @GetMapping("/staff")
    public String getDashboardStaffPage(Model model) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            var user = this.userService.getByEmail(authentication.getName());
            if (user != null && user.getStaff() != null && user.getStaff().getStaffType() == StaffType.SHIPPER) {
                return "redirect:/staff/shipper/dashboard";
            }
        }
        model.addAttribute("charData", this.orderService.getOrderStatisticData());
        model.addAttribute("cntUser", this.userService.countCustomer());
        model.addAttribute("cntOrder", this.orderRepository.count());
        return "staff/dashboard";
    }
}
