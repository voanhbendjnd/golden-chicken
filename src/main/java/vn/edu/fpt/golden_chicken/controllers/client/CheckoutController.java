package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckoutController {
    @GetMapping(value = "/checkout")
    public String create(Model model) {
        return "client/checkout";
    }
}
