package vn.edu.fpt.golden_chicken.controllers.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.edu.fpt.golden_chicken.services.VoucherService;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientVoucherController {
    VoucherService voucherService;

    @GetMapping("/vouchers")
    public String listVoucher(Model model) {
        long points = voucherService.getPoints();
        var vouchers = voucherService.getListVoucherForExchange();

        model.addAttribute("points", points);
        model.addAttribute("vouchers", vouchers);
        return "client/voucher/listVoucher";
    }
}
