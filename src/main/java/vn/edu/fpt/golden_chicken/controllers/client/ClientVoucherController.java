package vn.edu.fpt.golden_chicken.controllers.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        var myVouchers = voucherService.getMyVouchers();

        model.addAttribute("points", points);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("myVouchers", myVouchers);
        return "client/voucher/listVoucher";
    }

    @PostMapping("/vouchers/redeem")
    public String redeem(@RequestParam("voucherId") Long voucherId) {
        voucherService.redeemVoucher(voucherId);
        return "redirect:/vouchers";
    }

    @GetMapping("/vouchers/myVouchers")
    public String myVouchers(Model model) {
        model.addAttribute("points", voucherService.getPoints());
        model.addAttribute("myVouchers", voucherService.getMyVouchers());
        return "client/voucher/myVouchers";
    }

    @GetMapping("/vouchers/history")
    public String history(Model model) {
        model.addAttribute("points", voucherService.getPoints());
        model.addAttribute("history", voucherService.getRedeemHistory());
        return "client/voucher/historyRedeem";
    }

    @GetMapping("/vouchers/redeem/confirm")
    public String redeemConfirm(@RequestParam("voucherId") Long voucherId, Model model) {
        model.addAttribute("points", voucherService.getPoints());
        model.addAttribute("voucher", voucherService.getById(voucherId));
        return "client/voucher/redeemConfirm";
    }

}
