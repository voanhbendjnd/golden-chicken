package vn.edu.fpt.golden_chicken.controllers.staff;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.request.ComboDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResSingleProduct;
import vn.edu.fpt.golden_chicken.repositories.ComboDetailRepository;
import vn.edu.fpt.golden_chicken.services.CategoryService;
import vn.edu.fpt.golden_chicken.services.ComboDetailService;
import vn.edu.fpt.golden_chicken.services.ProductService;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Controller
@RequestMapping("/staff/combo")
public class ComboController {
    private final ProductService productService;
    private final ComboDetailRepository comboDetailRepository;
    private final CategoryService categoryService;
    private final ComboDetailService comboDetailService;

    public ComboController(CategoryService categoryService, ProductService productService,
            ComboDetailRepository comboDetailRepository, ComboDetailService comboDetailService) {
        this.productService = productService;
        this.comboDetailService = comboDetailService;
        this.categoryService = categoryService;
        this.comboDetailRepository = comboDetailRepository;
    }

    @GetMapping
    public String comboPage(Model model,
            @PageableDefault(size = DefineVariable.pageSize, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @Filter Specification<Product> spec) {
        var data = this.productService.fetchAllComboWithPagination(spec, pageable);
        model.addAttribute("products", data.getResult());
        model.addAttribute("meta", data.getMeta());
        return "staff/combo/table";
    }

    @GetMapping("/update/{id:[0-9]+}")
    public String updatePage(Model model, @PathVariable("id") Long id) {
        var combo = this.productService.findById(id);
        var comboDetails = this.comboDetailRepository.findByComboId(id);
        List<ResSingleProduct> currentItems = this.comboDetailService.getProductInCombo(id);

        Map<Long, Integer> currentProductQuantities = currentItems.stream()
                .collect(Collectors.toMap(ResSingleProduct::getId, ResSingleProduct::getQuantity));
        model.addAttribute("currentProductQuantities", currentProductQuantities);
        model.addAttribute("currentProductIds",
                comboDetails.stream().map(x -> x.getProduct().getId()).collect(Collectors.toList()));
        model.addAttribute("products", this.productService.fetchAllProductSingle());
        model.addAttribute("updateProduct", combo);
        model.addAttribute("categories", this.categoryService.fetchAll());
        // model.addAttribute("comboDetails", comboDetails);
        return "staff/combo/update";
    }

    @PostMapping("/update")
    public String update(Model model, @ModelAttribute("updateProduct") @Valid ComboDTO dto, BindingResult bd,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) List<MultipartFile> files)
            throws IOException, URISyntaxException {
        var combo = this.productService.findById(dto.getId());
        var comboDetails = this.comboDetailRepository.findByComboId(dto.getId());
        List<ResSingleProduct> currentItems = this.comboDetailService.getProductInCombo(dto.getId());

        Map<Long, Integer> currentProductQuantities = currentItems.stream()
                .collect(Collectors.toMap(ResSingleProduct::getId, ResSingleProduct::getQuantity));
        if (bd.hasErrors()) {
            model.addAttribute("currentProductIds",
                    comboDetails.stream().map(x -> x.getProduct().getId()).collect(Collectors.toList()));
            model.addAttribute("products", this.productService.fetchAllProductSingle());
            model.addAttribute("updateProduct", combo);
            model.addAttribute("currentProductQuantities", currentProductQuantities);

            model.addAttribute("categories", this.categoryService.fetchAll());
            return "staff/combo/update";
        }
        try {
            this.comboDetailService.update(dto, file, files);
        } catch (IOException ex) {
            model.addAttribute("currentProductQuantities", currentProductQuantities);

            model.addAttribute("error", ex.getMessage());
            model.addAttribute("currentProductIds",
                    comboDetails.stream().map(x -> x.getProduct().getId()).collect(Collectors.toList()));
            model.addAttribute("products", this.productService.fetchAllProductSingle());
            model.addAttribute("updateProduct", combo);
            model.addAttribute("categories", this.categoryService.fetchAll());
            return "staff/combo/update";
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("currentProductQuantities", currentProductQuantities);

            model.addAttribute("error", ex.getMessage());
            model.addAttribute("currentProductIds",
                    comboDetails.stream().map(x -> x.getProduct().getId()).collect(Collectors.toList()));
            model.addAttribute("products", this.productService.fetchAllProductSingle());
            model.addAttribute("updateProduct", combo);
            model.addAttribute("categories", this.categoryService.fetchAll());
            return "staff/combo/update";
        }
        return "redirect:/staff/combo";
    }

    @PostMapping("/delete/{id:[0-9]+}")
    public String delete(@PathVariable("id") Long id) {
        this.productService.delete(id);
        return "redirect:/staff/combo";
    }
}
