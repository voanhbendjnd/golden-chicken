package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AddressErrorController {
    @GetMapping("address/config")
    public String errorAddressPage(RedirectAttributes ra) {
        ra.addFlashAttribute("msgWarning", "Bạn cần thêm ít nhất 1 địa chỉ để thanh toán!");
        return "redirect:/addresses";
    }
}
