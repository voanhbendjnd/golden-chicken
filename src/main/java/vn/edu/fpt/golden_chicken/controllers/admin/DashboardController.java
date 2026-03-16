package vn.edu.fpt.golden_chicken.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@Controller("adminDashboardController")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DashboardController {
    UserService userService;

    @GetMapping("/admin")
    public String getDashboardAdminPage() {
        return "redirect:/admin/user";
    }

    @GetMapping("/staff")
    public String getDashboardStaffPage(Model model) {
        var user = this.userService.getUserInContext();
        var staff = user.getStaff();
        if (staff != null) {
            var staffType = staff.getStaffType();
            if (staffType == StaffType.SHIPPER) {
                return "redirect:/staff/shipper/dashboard";
            }
            if (staffType == StaffType.RECEPTIONIST) {
                return "redirect:/staff/order";
            }
            if (staffType == StaffType.MANAGER) {
                return "redirect:/staff/dashboard";
            } else {
                return "redirect:/staff/dashboard";
            }
        }
        return "redirect:/";

    }
}
