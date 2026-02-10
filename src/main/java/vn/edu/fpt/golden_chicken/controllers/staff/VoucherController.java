package vn.edu.fpt.golden_chicken.controllers.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.golden_chicken.domain.request.VoucherCreateDTO;
import vn.edu.fpt.golden_chicken.domain.request.VoucherUpdateDTO;
import vn.edu.fpt.golden_chicken.services.VoucherService;

@Controller
@RequestMapping("/staff/voucher")
public class VoucherController {
    private final VoucherService service;

    public VoucherController(VoucherService service) {
        this.service = service;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("voucher", new VoucherCreateDTO());
        return "staff/voucher/create";
    }

    @PostMapping("/create")
    public String create(
            @ModelAttribute("voucher") VoucherCreateDTO dto,
            Model model
    ) {
        try {
            service.createVoucher(dto);
            return "redirect:/staff/voucher/list";

        } catch (IllegalArgumentException e) {
            // báo lỗi ra view
            model.addAttribute("error", e.getMessage());
            model.addAttribute("voucher", dto);
            return "staff/voucher/create";
        }
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("vouchers", service.getAll());
        return "staff/voucher/list";
    }

    // chi tiet voucher
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("voucher", service.getById(id));
        return "staff/voucher/detail";
    }

    //update
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("voucher", service.getById(id));
        return "staff/voucher/edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") Long id,
                         @ModelAttribute VoucherUpdateDTO dto) {
        service.updateVoucher(id, dto);
        return "redirect:/staff/voucher/list";
    }

    @GetMapping("/disable/{id}")
    public String disable(@PathVariable("id") Long id) {
        service.disableVoucher(id);
        return "redirect:/staff/voucher/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable("id") Long id) {
        service.deleteVoucher(id);
        return "redirect:/staff/voucher/list";
    }

}

