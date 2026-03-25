package vn.edu.fpt.golden_chicken.controllers.client;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.golden_chicken.domain.request.AddressFormDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResAddress;
import vn.edu.fpt.golden_chicken.services.AddressServices;

import java.util.ArrayList;
import java.util.List;

@Controller
@PreAuthorize("hasRole('CUSTOMER')")
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
    public String createForm(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            Model model) {

        model.addAttribute("addressForm", new AddressFormDTO());
        model.addAttribute("isEdit", false);

        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        model.addAttribute("orderId", orderId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("productVoucherId", productVoucherId);
        model.addAttribute("shippingVoucherId", shippingVoucherId);

        return "client/address/createAddress";
    }

    @PostMapping("/addresses/new")
    public String create(
            @Valid @ModelAttribute("addressForm") AddressFormDTO form,
            BindingResult result,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("productId", productId);
            model.addAttribute("productIds", productIds);
            model.addAttribute("orderId", orderId);
            model.addAttribute("quantity", quantity);
            model.addAttribute("productVoucherId", productVoucherId);
            model.addAttribute("shippingVoucherId", shippingVoucherId);
            return "client/address/createAddress";
        }

        addressServices.createMyAddress(form);

        if (productId != null || (productIds != null && !productIds.isEmpty()) || orderId != null) {
            StringBuilder url = new StringBuilder("redirect:/checkout/addresses?");
            if (productId != null) {
                url.append("productId=").append(productId);
                if (quantity != null) {
                    url.append("&quantity=").append(quantity);
                }
            }
            if (productIds != null && !productIds.isEmpty()) {
                if (productId != null)
                    url.append("&");
                url.append("productIds=").append(productIds);
            }
            if (orderId != null) {
                if (productId != null || (productIds != null && !productIds.isEmpty()))
                    url.append("&");
                url.append("orderId=").append(orderId);
            }
            if (productVoucherId != null) {
                url.append("&productVoucherId=").append(productVoucherId);
            }
            if (shippingVoucherId != null) {
                url.append("&shippingVoucherId=").append(shippingVoucherId);
            }
            return url.toString();
        }

        return "redirect:/addresses";
    }

    @GetMapping("/addresses/edit")
    public String editForm(
            @RequestParam("id") Long id,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            Model model) {
        AddressFormDTO form = addressServices.getMyAddressForm(id);
        if (form == null)
            return "redirect:/addresses";

        model.addAttribute("addressId", id);
        model.addAttribute("addressForm", form);
        model.addAttribute("isEdit", true);

        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        model.addAttribute("orderId", orderId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("productVoucherId", productVoucherId);
        model.addAttribute("shippingVoucherId", shippingVoucherId);

        return "client/address/createAddress";
    }

    @PostMapping("/addresses/update")
    public String update(
            @Valid @ModelAttribute("addressForm") AddressFormDTO form,
            BindingResult result,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", productId);
            model.addAttribute("productIds", productIds);
            model.addAttribute("orderId", orderId);
            model.addAttribute("quantity", quantity);
            model.addAttribute("productVoucherId", productVoucherId);
            model.addAttribute("shippingVoucherId", shippingVoucherId);
            return "client/address/createAddress";
        }

        addressServices.updateUserAddress(form);

        if (productId != null || (productIds != null && !productIds.isEmpty()) || orderId != null) {
            StringBuilder url = new StringBuilder("redirect:/checkout/addresses?");
            if (productId != null) {
                url.append("productId=").append(productId);
                if (quantity != null) {
                    url.append("&quantity=").append(quantity);
                }
            }
            if (productIds != null && !productIds.isEmpty()) {
                if (productId != null)
                    url.append("&");
                url.append("productIds=").append(productIds);
            }
            if (orderId != null) {
                if (productId != null || (productIds != null && !productIds.isEmpty()))
                    url.append("&");
                url.append("orderId=").append(orderId);
            }
            if (productVoucherId != null) {
                url.append("&productVoucherId=").append(productVoucherId);
            }
            if (shippingVoucherId != null) {
                url.append("&shippingVoucherId=").append(shippingVoucherId);
            }
            return url.toString();
        }

        return "redirect:/addresses";
    }

    @PostMapping("/addresses/{id}/default")
    public String setDefault(@PathVariable Long id) {
        addressServices.setCurrentUserDefaultAddress(id);
        return "redirect:/addresses";
    }

    @GetMapping("/addresses/{id}/delete")
    public String confirmDelete(
            @PathVariable Long id,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            Model model) {
        var address = addressServices.findById(id);
        if (address == null) {
            return "redirect:/addresses";
        }
        model.addAttribute("address", address);
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        model.addAttribute("orderId", orderId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("productVoucherId", productVoucherId);
        model.addAttribute("shippingVoucherId", shippingVoucherId);
        return "client/address/deleteAddress";
    }

    @PostMapping("/addresses/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId) {

        addressServices.deleteMyAddress(id);

        if (productId != null || (productIds != null && !productIds.isEmpty()) || orderId != null) {
            StringBuilder url = new StringBuilder("redirect:/checkout/addresses?");
            if (productId != null) {
                url.append("productId=").append(productId);
                if (quantity != null) {
                    url.append("&quantity=").append(quantity);
                }
            }
            if (productIds != null && !productIds.isEmpty()) {
                if (productId != null)
                    url.append("&");
                url.append("productIds=").append(productIds);
            }
            if (orderId != null) {
                if (productId != null || (productIds != null && !productIds.isEmpty()))
                    url.append("&");
                url.append("orderId=").append(orderId);
            }
            if (productVoucherId != null) {
                url.append("&productVoucherId=").append(productVoucherId);
            }
            if (shippingVoucherId != null) {
                url.append("&shippingVoucherId=").append(shippingVoucherId);
            }
            return url.toString();
        }

        return "redirect:/addresses";
    }
}