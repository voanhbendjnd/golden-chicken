package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.mail.Address;
import jakarta.validation.Valid;
import vn.edu.fpt.golden_chicken.domain.request.AddressFormDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResAddress;
import vn.edu.fpt.golden_chicken.services.AddressServices;

@Controller
public class AddressController {

    private final AddressServices addressServices;

    public AddressController(AddressServices addressServices) {
        this.addressServices = addressServices;
    }

    @GetMapping("/addresses")
    public String addressBook(Model model) {
        var addresses = addressServices.getAllAddresses();
        var defaultAddress = addressServices.getDefaultAddress();
        List<ResAddress> additionalAddresses = new ArrayList<>();

        for (ResAddress a : addresses) {
            if (a.getIsDefault() == null || !a.getIsDefault()) {
                additionalAddresses.add(a);
            }
        }

        model.addAttribute("additionalAddresses", additionalAddresses);
        model.addAttribute("defaultAddress", defaultAddress);
        return "client/address/listAddress";
    }

    @GetMapping("/addresses/new")
    public String createForm(Model model) {
        model.addAttribute("addressForm", new AddressFormDTO());
        model.addAttribute("isEdit", false);
        return "client/address/createAddress";
    }

    @PostMapping("/addresses/new")
    public String create(
            @Valid @ModelAttribute("addressForm") AddressFormDTO form,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "client/address/createAddress";
        }
        addressServices.createMyAddress(form);
        return "redirect:/addresses";
    }

    @PostMapping("/addresses/edit")
    public String editForm(@RequestParam("id") Long id, Model model) {
        AddressFormDTO form = addressServices.getMyAddressForm(id);
        if (form == null)
            return "redirect:/addresses";

        model.addAttribute("addressId", id);
        model.addAttribute("addressForm", form);
        model.addAttribute("isEdit", true);
        return "client/address/createAddress";
    }

    @PostMapping("/addresses/update")
    public String update(
            @Valid @ModelAttribute("addressForm") AddressFormDTO form,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "client/address/createAddress";
        }
        addressServices.updateUserAddress(form);
        return "redirect:/addresses";
    }

    @PostMapping("/addresses/{id}/default")
    public String setDefault(@PathVariable Long id) {
        addressServices.setCurrentUserDefaultAddress(id);
        return "redirect:/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String delete(@PathVariable Long id) {
        addressServices.deleteMyAddress(id);
        return "redirect:/addresses";
    }


}
