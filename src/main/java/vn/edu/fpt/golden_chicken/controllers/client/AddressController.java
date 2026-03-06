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
    public String createForm(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds, // Nhận dạng String để dễ truyền qua URL
            Model model) {

        model.addAttribute("addressForm", new AddressFormDTO());
        model.addAttribute("isEdit", false);

        // Đẩy thông tin giỏ hàng ra để hiển thị nút "Quay lại"
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);

        return "client/address/createAddress";
    }

    @PostMapping("/addresses/new")
    public String create(
            @Valid @ModelAttribute("addressForm") AddressFormDTO form,
            BindingResult result,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("productId", productId);
            model.addAttribute("productIds", productIds);
            return "client/address/createAddress";
        }

        addressServices.createMyAddress(form);

        // LOGIC ĐIỀU HƯỚNG THÔNG MINH:
        // Nếu có dữ liệu sản phẩm, quay lại trang CHỌN ĐỊA CHỈ THANH TOÁN
        if (productId != null || (productIds != null && !productIds.isEmpty())) {
            StringBuilder url = new StringBuilder("redirect:/checkout/addresses?");
            if (productId != null) url.append("productId=").append(productId);
            if (productIds != null && !productIds.isEmpty()) {
                if (productId != null) url.append("&");
                url.append("productIds=").append(productIds);
            }
            return url.toString();
        }

        // Luồng bình thường (vào từ profile) -> Về Sổ địa chỉ
        return "redirect:/addresses";
    }

    // Đổi từ Post sang Get để nhận tham số từ link dễ hơn
    @GetMapping("/addresses/edit")
    public String editForm(
            @RequestParam("id") Long id,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            Model model) {
        AddressFormDTO form = addressServices.getMyAddressForm(id);
        if (form == null) return "redirect:/addresses";

        model.addAttribute("addressId", id);
        model.addAttribute("addressForm", form);
        model.addAttribute("isEdit", true);

        // Đẩy thông tin giỏ hàng vào model để Form edit biết đường quay về
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);

        return "client/address/createAddress";
    }

    @PostMapping("/addresses/update")
    public String update(
            @Valid @ModelAttribute("addressForm") AddressFormDTO form,
            BindingResult result,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", productId);
            model.addAttribute("productIds", productIds);
            return "client/address/createAddress";
        }

        addressServices.updateUserAddress(form);

        // LOGIC ĐIỀU HƯỚNG TƯƠNG TỰ NHƯ TẠO MỚI
        if (productId != null || (productIds != null && !productIds.isEmpty())) {
            StringBuilder url = new StringBuilder("redirect:/checkout/addresses?");
            if (productId != null) url.append("productId=").append(productId);
            if (productIds != null && !productIds.isEmpty()) {
                if (productId != null) url.append("&");
                url.append("productIds=").append(productIds);
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
            Model model) {
        var address = addressServices.findById(id);
        if (address == null) {
            return "redirect:/addresses";
        }
        model.addAttribute("address", address);
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        return "client/address/deleteAddress";
    }

    @PostMapping("/addresses/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds) {

        addressServices.deleteMyAddress(id);

        // Điều hướng về trang chọn địa chỉ nếu đang trong luồng thanh toán
        if (productId != null || (productIds != null && !productIds.isEmpty())) {
            StringBuilder url = new StringBuilder("redirect:/checkout/addresses?");
            if (productId != null) url.append("productId=").append(productId);
            if (productIds != null && !productIds.isEmpty()) {
                if (productId != null) url.append("&");
                url.append("productIds=").append(productIds);
            }
            return url.toString();
        }

        return "redirect:/addresses";
    }
}