package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.golden_chicken.services.AddressServices;

@Controller
public class AddressController {

    private final AddressServices addressServices;

    public AddressController(AddressServices addressServices) {
        this.addressServices = addressServices;
    }

    @GetMapping("/addresses")
    public String addressList(Model model) {
        model.addAttribute("addresses", addressServices.getMyAddresses());
        model.addAttribute("defaultAddress", addressServices.getMyDefaultAddress());
        return "client/address/list";
    }
}
