package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.fpt.golden_chicken.domain.response.ProductSearchSuggestionDTO;
import vn.edu.fpt.golden_chicken.services.ProductService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Autowired
    private ProductService productService;

    @GetMapping("/suggestions")
    public List<ProductSearchSuggestionDTO> getSuggestions(@RequestParam String query) {
        if (query == null || query.trim().length() < 1)
            return Collections.emptyList();
        return productService.searchByName(query.trim());
    }
}
