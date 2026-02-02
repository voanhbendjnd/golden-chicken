package vn.edu.fpt.golden_chicken.controllers.staff;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

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
import vn.edu.fpt.golden_chicken.domain.request.ProductDTO;
import vn.edu.fpt.golden_chicken.services.CategoryService;
import vn.edu.fpt.golden_chicken.services.ProductService;

@Controller
@RequestMapping("/staff/product")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @PostMapping("/{id:[0-9]+}")
    public String delete(@PathVariable("id") Long id) {
        this.productService.delete(id);
        return "redirect:/staff/product";
    }

    @GetMapping
    public String getProductTablePage(Model model,
            @PageableDefault(size = DefineVariable.pageSize, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @Filter Specification<Product> spec) {
        var data = this.productService.fetchAllWithPagination(pageable, spec);
        model.addAttribute("meta", data.getMeta());
        model.addAttribute("products", data.getResult());
        return "staff/product/table";
    }

    @GetMapping("/create")
    public String getCreatePage(Model model) {
        model.addAttribute("categories", this.categoryService.fetchAll());
        model.addAttribute("product", new ProductDTO());
        return "staff/product/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("product") @Valid ProductDTO productDTO, BindingResult bd,
            @RequestParam("thumbnailFile") MultipartFile thumbnailFile,
            @RequestParam("galleryFiles") List<MultipartFile> galleryFiles, Model model)
            throws IOException, URISyntaxException {
        if (bd.hasErrors()) {
            model.addAttribute("categories", this.categoryService.fetchAll());
            return "staff/product/create";
        }
        try {
            this.productService.create(productDTO, galleryFiles, thumbnailFile);
            return "redirect:/staff/product";
        } catch (IOException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            // bd.rejectValue("errorMessage", "IO Exception", ex.getMessage());
            model.addAttribute("categories", this.categoryService.fetchAll());
            return "staff/prouduct/create";
        }

    }

    @GetMapping("/{id:[0-9]+}")
    public String detailPage(Model model, @PathVariable("id") Long id) {
        model.addAttribute("product", this.productService.findById(id));
        return "staff/product/detail";
    }

    @GetMapping("/update/{id:[0-9]+}")
    public String updatePage(Model model, @PathVariable("id") Long id) {
        var product = this.productService.findById(id);
        model.addAttribute("categories", this.categoryService.fetchAll());
        model.addAttribute("product", product);
        model.addAttribute("img", product.getImg());
        model.addAttribute("imgs", product.getImgs());
        return "staff/product/update";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("product") @Valid ProductDTO dto, BindingResult bd,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) List<MultipartFile> files, Model model)
            throws IOException, URISyntaxException {
        if (bd.hasErrors()) {
            model.addAttribute("categories", this.categoryService.fetchAll());
            return "staff/product/update";
        }
        try {
            this.productService.update(dto, file, files);

        } catch (IOException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("categories", this.categoryService.fetchAll());
            return "staff/product/update";
        }
        return "redirect:/staff/product";

    }
}
