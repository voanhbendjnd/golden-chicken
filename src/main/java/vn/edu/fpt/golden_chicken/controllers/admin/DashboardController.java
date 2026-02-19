package vn.edu.fpt.golden_chicken.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.services.UserService;

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
        model.addAttribute("charData", this.orderService.getOrderStatisticData());
        model.addAttribute("cntUser", this.userService.countCustomer());
        model.addAttribute("cntOrder", this.orderRepository.count());
        return "staff/dashboard";
    }
}
