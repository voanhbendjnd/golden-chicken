package vn.edu.fpt.golden_chicken.controllers.staff;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.golden_chicken.domain.request.VoucherCreateDTO;
import vn.edu.fpt.golden_chicken.domain.request.VoucherUpdateDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResVoucher;
import vn.edu.fpt.golden_chicken.services.VoucherService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
            @Valid @ModelAttribute("voucher") VoucherCreateDTO dto,
            BindingResult result,
            Model model) {

        // 🔥 validate thời gian
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        // if (dto.getStartAt() != null && dto.getStartAt().isBefore(now)) {
        // result.rejectValue("startAt", "error.startAt",
        // "Start time must be now or in the future");
        // }
        if (dto.getEndAt() != null && dto.getEndAt().isBefore(now)) {
            result.rejectValue("endAt", "error.endAt",
                    "End time must be now or in the future");
        }
        // 1. Lỗi validation DTO
        if (result.hasErrors()) {
            return "staff/voucher/create";
        }

        try {
            service.createVoucher(dto);
            return "redirect:/staff/voucher/list";
        } catch (IllegalArgumentException ex) {

            // Đưa message ra view
            model.addAttribute("errorMessage", ex.getMessage());

            return "staff/voucher/create";
        }
    }

    @GetMapping("/list")
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "5") int size,
                       @RequestParam(required = false) String searchCode,
                       Model model) {

        service.refreshExpiredStatus();

        Page<ResVoucher> voucherPage; // thêm dòng này

        if (searchCode != null && !searchCode.trim().isEmpty()) {
            voucherPage = service.searchByCode(searchCode, page, size); // thêm đoạn search
        } else {
            voucherPage = service.getAll(page, size); // giữ code cũ
        }

        model.addAttribute("vouchers", voucherPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("searchCode", searchCode);

        return "staff/voucher/list";
    }

    @GetMapping("/detail/{id:[0-9]+}")
    public String detail(@PathVariable("id") Long id, Model model) {
        service.refreshExpiredStatus();
        model.addAttribute("voucher", service.getById(id));
        return "staff/voucher/detail";
    }

    @GetMapping("/edit/{id:[0-9]+}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("voucher", service.getById(id));
        return "staff/voucher/edit";
    }

    @PostMapping("/edit/{id:[0-9]+}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("voucher") VoucherUpdateDTO dto,
            BindingResult result,
            Model model) {

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        if (dto.getEndAt() != null && dto.getEndAt().isBefore(now)) {
            result.rejectValue("endAt", "error.endAt",
                    "End time must be now or in the future");
        }
        if (result.hasErrors()) {
            return "staff/voucher/edit";
        }

        try {
            service.updateVoucher(dto);
            return "redirect:/staff/voucher/list";
        } catch (IllegalArgumentException ex) {

            model.addAttribute("errorMessage", ex.getMessage());

            return "staff/voucher/edit";
        }
    }

    @GetMapping("/disable/{id:[0-9]+}")
    public String disable(@PathVariable("id") Long id) {
        service.disableVoucher(id);
        return "redirect:/staff/voucher/list";
    }

    @GetMapping("/delete/{id:[0-9]+}")
    public String deleteVoucher(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            service.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("success", "Xóa voucher thành công");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/staff/voucher/list";
    }

}
