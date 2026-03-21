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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.BadWord;
import vn.edu.fpt.golden_chicken.domain.request.BadWordDTO;
import vn.edu.fpt.golden_chicken.services.BadWordService;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/staff/badword")
public class BadWordController {
    BadWordService badWordService;

    @GetMapping()
    public String tablePage(Model model, @Filter Specification<BadWord> spec,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        var data = this.badWordService.fetchAllWithPagination(spec, pageable);
        model.addAttribute("meta", data.getMeta());
        model.addAttribute("badwords", data.getResult());
        return "staff/badword/table";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("badword", new BadWordDTO(null, null, null));
        return "staff/badword/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("badword") BadWordDTO dto, BindingResult br, Model model,
            RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "staff/badword/create";
        }
        try {
            this.badWordService.create(dto);
            ra.addFlashAttribute("msg", "Tạo mới thành công!");
        } catch (DataInvalidException de) {
            br.rejectValue("word", "CONFLICT", de.getMessage());
            // model.addAttribute("errorMessage", de.getMessage());
            return "staff/badword/create";
        }
        return "redirect:/staff/badword";
    }

    @GetMapping("/update/{id:[0-9]+}")
    public String updatePage(Model model, @PathVariable("id") Long id) {
        model.addAttribute("badword", this.badWordService.fetchById(id));
        return "staff/badword/update";
    }

    @PostMapping("/update")
    public String update(Model model, @ModelAttribute("badword") BadWordDTO dto, BindingResult br,
            RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "staff/badword/update";
        }
        try {
            this.badWordService.update(dto);
            ra.addFlashAttribute("msg", "Cập nhật thành công!");
        } catch (DataInvalidException ex) {
            br.rejectValue("word", "CONFLICT", ex.getMessage());
            // model.addAttribute("errorMessage", ex.getMessage());
            return "staff/badword/update";
        } catch (ResourceNotFoundException ex) {
            br.rejectValue("errorMessage", ex.getMessage());
            return "staff/badword/update";
        }
        return "redirect:/staff/badword";
    }

    @PostMapping("/delete/{id:[0-9]+}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            this.badWordService.delete(id);
            ra.addFlashAttribute("msg", "Xóa thành công!");
        } catch (ResourceNotFoundException de) {
            ra.addFlashAttribute("errorMessage", "Xóa thất bại!");
            return "redirect:/staff/badword";

        }

        return "redirect:/staff/badword";
    }

    @PostMapping("/status/{id:[0-9]+}")
    public String revertStatus(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            this.badWordService.revertStatus(id);
            ra.addFlashAttribute("msg", "Thay đổi trạng thái thành công!");
        } catch (ResourceNotFoundException re) {
            ra.addFlashAttribute("errorMessage", "Thay đổi trạng thái thất bại");
            return "redirect:/staff/badword";

        }
        return "redirect:/staff/badword";
    }

}
