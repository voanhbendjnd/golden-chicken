package vn.edu.fpt.golden_chicken.controllers.staff;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.ShippingFee;
import vn.edu.fpt.golden_chicken.domain.request.ShippingFeeDTO;
import vn.edu.fpt.golden_chicken.services.ShippingFeeService;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/staff/shippingFee")
public class StaffShippingFeeController {
    ShippingFeeService shippingFeeService;

    @GetMapping
    public String sfPage(Model model, @Filter Specification<ShippingFee> spec,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var data = this.shippingFeeService.fetchAllWithPagination(spec, pageable);
        model.addAttribute("shippingFees", data.getResult());
        model.addAttribute("meta", data.getMeta());
        return "staff/sf/table";
    }

    @GetMapping("/create")
    public String sfCreatePage(Model model) {
        model.addAttribute("shippingFee", new ShippingFeeDTO());
        return "staff/sf/create";
    }

    @PostMapping("create")
    public String sfCreate(@ModelAttribute("shippingFee") @Valid ShippingFeeDTO shippingFeeDTO, RedirectAttributes ra,
            BindingResult br, Model model) {
        if (br.hasErrors()) {
            return "staff/sf/create";

        }
        try {
            this.shippingFeeService.create(shippingFeeDTO);
            ra.addFlashAttribute("msg", "Tạo mới thành công!");

        } catch (DataInvalidException de) {
            br.rejectValue("ward", "error.user", de.getMessage());
            return "staff/sf/create";
        }

        return "redirect:/staff/shippingFee";

    }

    @GetMapping("/update/{id:[0-9]+}")
    public String sfUpdatePage(Model model, @PathVariable("id") Long id) {
        model.addAttribute("shippingFee", this.shippingFeeService.fetchById(id));
        return "staff/sf/update";
    }

    @PostMapping("/update")
    public String sfUpdate(@ModelAttribute("shippingFee") ShippingFeeDTO dto, RedirectAttributes ra, Model model,
            BindingResult br) {
        if (br.hasErrors()) {
            return "staff/sf/update";
        }
        try {
            this.shippingFeeService.update(dto);
            ra.addFlashAttribute("msg", "Cập nhật thành công!");

        } catch (DataInvalidException de) {
            br.rejectValue("ward", "error.user", de.getMessage());
            return "staff/sf/update";
        } catch (ResourceNotFoundException de) {
            br.rejectValue("ward", "error.user", de.getMessage());
            return "staff/sf/update";
        }
        return "redirect:/staff/shippingFee";
    }

    @PostMapping("/delete/{id:[0-9]+}")
    public String stDelete(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            this.shippingFeeService.delete(id);
            ra.addFlashAttribute("msg", "Xóa thành công!");
            return "redirect:/staff/shippingFee";
        } catch (ResourceNotFoundException rf) {
            ra.addFlashAttribute("errorMessage", rf.getMessage());
            return "redirect:/staff/shippingFee";
        }
    }
}
