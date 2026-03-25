package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import vn.edu.fpt.golden_chicken.domain.request.AddressFormDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResAddress;
import vn.edu.fpt.golden_chicken.services.AddressServices;

@Controller
@RequestMapping("/addresses")
@PreAuthorize("hasRole('CUSTOMER')")
public class AddressController {

    private final AddressServices addressServices;

    public AddressController(AddressServices addressServices) {
        this.addressServices = addressServices;
    }

    @GetMapping
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

    @RequestMapping(value = "/new/init", method = { org.springframework.web.bind.annotation.RequestMethod.GET,
            org.springframework.web.bind.annotation.RequestMethod.POST })
    public String createForm(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            Model model) {

        if (productId == null && model.containsAttribute("productId") && model.asMap().get("productId") != null) {
            productId = Long.valueOf(model.asMap().get("productId").toString());
        }
        if (productIds == null && model.containsAttribute("productIds") && model.asMap().get("productIds") != null) {
            productIds = model.asMap().get("productIds").toString();
        }
        if (orderId == null && model.containsAttribute("orderId") && model.asMap().get("orderId") != null) {
            orderId = Long.valueOf(model.asMap().get("orderId").toString());
        }
        if (quantity == null && model.containsAttribute("quantity") && model.asMap().get("quantity") != null) {
            quantity = Integer.valueOf(model.asMap().get("quantity").toString());
        } else if (quantity == null) {
            quantity = 1;
        }

        model.addAttribute("addressForm", new AddressFormDTO());
        model.addAttribute("isEdit", false);

        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        model.addAttribute("orderId", orderId);
        model.addAttribute("productVoucherId", productVoucherId);
        model.addAttribute("shippingVoucherId", shippingVoucherId);
        model.addAttribute("quantity", quantity);

        return "client/address/createAddress";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("addressForm") AddressFormDTO form,
            BindingResult result,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("productId", productId);
            model.addAttribute("productIds", productIds);
            model.addAttribute("orderId", orderId);
            model.addAttribute("productVoucherId", productVoucherId);
            model.addAttribute("shippingVoucherId", shippingVoucherId);
            model.addAttribute("quantity", quantity);

            return "client/address/createAddress";
        }

        addressServices.createMyAddress(form);

        if (productId != null || (productIds != null && !productIds.isEmpty()) || orderId != null) {
            redirectAttributes.addFlashAttribute("productId", productId);
            redirectAttributes.addFlashAttribute("productIds", productIds);
            redirectAttributes.addFlashAttribute("orderId", orderId);
            redirectAttributes.addFlashAttribute("productVoucherId", productVoucherId);
            redirectAttributes.addFlashAttribute("shippingVoucherId", shippingVoucherId);
            redirectAttributes.addFlashAttribute("quantity", quantity);
            return "redirect:/checkout/addresses";
        }

        return "redirect:/addresses";
    }

    @RequestMapping(value = "/edit/init", method = { org.springframework.web.bind.annotation.RequestMethod.GET,
            org.springframework.web.bind.annotation.RequestMethod.POST })
    public String editForm(
            @RequestParam("id") Long id,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            Model model) {

        if (productId == null && model.containsAttribute("productId") && model.asMap().get("productId") != null) {
            productId = Long.valueOf(model.asMap().get("productId").toString());
        }
        if (productIds == null && model.containsAttribute("productIds") && model.asMap().get("productIds") != null) {
            productIds = model.asMap().get("productIds").toString();
        }
        if (orderId == null && model.containsAttribute("orderId") && model.asMap().get("orderId") != null) {
            orderId = Long.valueOf(model.asMap().get("orderId").toString());
        }
        if (quantity == null && model.containsAttribute("quantity") && model.asMap().get("quantity") != null) {
            quantity = Integer.valueOf(model.asMap().get("quantity").toString());
        } else if (quantity == null) {
            quantity = 1;
        }
        if (productVoucherId == null && model.containsAttribute("productVoucherId")
                && model.asMap().get("productVoucherId") != null) {
            productVoucherId = Long.valueOf(model.asMap().get("productVoucherId").toString());
        }
        if (shippingVoucherId == null && model.containsAttribute("shippingVoucherId")
                && model.asMap().get("shippingVoucherId") != null) {
            shippingVoucherId = Long.valueOf(model.asMap().get("shippingVoucherId").toString());
        }

        AddressFormDTO form = addressServices.getMyAddressForm(id);
        if (form == null)
            return "redirect:/addresses";

        model.addAttribute("addressId", id);
        model.addAttribute("addressForm", form);
        model.addAttribute("isEdit", true);

        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        model.addAttribute("orderId", orderId);
        model.addAttribute("productVoucherId", productVoucherId);
        model.addAttribute("shippingVoucherId", shippingVoucherId);
        model.addAttribute("quantity", quantity);

        return "client/address/createAddress";
    }

    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("addressForm") AddressFormDTO form,
            BindingResult result,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", productId);
            model.addAttribute("productIds", productIds);
            model.addAttribute("orderId", orderId);
            model.addAttribute("productVoucherId", productVoucherId);
            model.addAttribute("shippingVoucherId", shippingVoucherId);
            model.addAttribute("quantity", quantity);

            return "client/address/createAddress";
        }

        addressServices.updateUserAddress(form);

        if (productId != null || (productIds != null && !productIds.isEmpty()) || orderId != null) {
            redirectAttributes.addFlashAttribute("productId", productId);
            redirectAttributes.addFlashAttribute("productIds", productIds);
            redirectAttributes.addFlashAttribute("orderId", orderId);
            redirectAttributes.addFlashAttribute("productVoucherId", productVoucherId);
            redirectAttributes.addFlashAttribute("shippingVoucherId", shippingVoucherId);
            redirectAttributes.addFlashAttribute("quantity", quantity);
            return "redirect:/checkout/addresses";
        }

        return "redirect:/addresses";
    }

    @PostMapping("/{id}/default")
    public String setDefault(@PathVariable Long id) {
        addressServices.setCurrentUserDefaultAddress(id);
        return "redirect:/addresses";
    }

    @GetMapping("/{id}/delete")
    @PostMapping("/{id}/delete/init")
    public String confirmDelete(
            @PathVariable Long id,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            Model model) {

        if (productId == null && model.containsAttribute("productId") && model.asMap().get("productId") != null) {
            productId = Long.valueOf(model.asMap().get("productId").toString());
        }
        if (productIds == null && model.containsAttribute("productIds") && model.asMap().get("productIds") != null) {
            productIds = model.asMap().get("productIds").toString();
        }
        if (orderId == null && model.containsAttribute("orderId") && model.asMap().get("orderId") != null) {
            orderId = Long.valueOf(model.asMap().get("orderId").toString());
        }
        if (quantity == null && model.containsAttribute("quantity") && model.asMap().get("quantity") != null) {
            quantity = Integer.valueOf(model.asMap().get("quantity").toString());
        }
        if (productVoucherId == null && model.containsAttribute("productVoucherId")
                && model.asMap().get("productVoucherId") != null) {
            productVoucherId = Long.valueOf(model.asMap().get("productVoucherId").toString());
        }
        if (shippingVoucherId == null && model.containsAttribute("shippingVoucherId")
                && model.asMap().get("shippingVoucherId") != null) {
            shippingVoucherId = Long.valueOf(model.asMap().get("shippingVoucherId").toString());
        }
        var address = addressServices.findById(id);
        if (address == null) {
            return "redirect:/addresses";
        }
        model.addAttribute("address", address);
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        model.addAttribute("orderId", orderId);
        model.addAttribute("productVoucherId", productVoucherId);
        model.addAttribute("shippingVoucherId", shippingVoucherId);
        return "client/address/deleteAddress";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            RedirectAttributes redirectAttributes) {

        addressServices.deleteMyAddress(id);

        if (productId != null || (productIds != null && !productIds.isEmpty()) || orderId != null) {
            redirectAttributes.addFlashAttribute("productId", productId);
            redirectAttributes.addFlashAttribute("productIds", productIds);
            redirectAttributes.addFlashAttribute("orderId", orderId);
            redirectAttributes.addFlashAttribute("productVoucherId", productVoucherId);
            redirectAttributes.addFlashAttribute("shippingVoucherId", shippingVoucherId);
            redirectAttributes.addFlashAttribute("quantity", quantity);
            return "redirect:/checkout/addresses";
        }

        return "redirect:/addresses";
    }
}