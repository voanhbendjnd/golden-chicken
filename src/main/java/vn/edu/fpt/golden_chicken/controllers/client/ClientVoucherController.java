package vn.edu.fpt.golden_chicken.controllers.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.services.ProfileService;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientVoucherController {
    ProfileService profileService;
    CustomerRepository customerRepository;

    @GetMapping("/vouchers")
    public String listVoucher(Model model) {
        long points = 0L;
        var currentUser = profileService.getCurrentUser();
        if (currentUser != null) {
            var customer = customerRepository.findById(currentUser.getId()).orElse(null);
            if (customer != null && customer.getPoint() != null) {
                points = customer.getPoint();
            }
        }
        model.addAttribute("points", points);
        return "client/voucher/listVoucher";
    }
}
