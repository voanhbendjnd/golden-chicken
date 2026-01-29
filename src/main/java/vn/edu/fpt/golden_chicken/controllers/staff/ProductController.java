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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.turkraft.springfilter.boot.Filter;

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
        model.addAttribute("categories", this.categoryService.fectchAll());
        model.addAttribute("product", new ProductDTO());
        return "staff/product/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("product") ProductDTO productDTO,
            @RequestParam("thumbnailFile") MultipartFile thumbnailFile,
            @RequestParam("galleryFiles") List<MultipartFile> galleryFiles) throws IOException, URISyntaxException {
        this.productService.create(productDTO, galleryFiles, thumbnailFile);
        return "redirect:/staff/product";
    }

}
