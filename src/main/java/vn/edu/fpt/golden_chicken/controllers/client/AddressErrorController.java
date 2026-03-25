package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AddressErrorController {
    @GetMapping("address/config")
    public String errorAddressPage() {
        return "error/address.error";
    }
}
