package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.response.ProductSearchSuggestionDTO;
import vn.edu.fpt.golden_chicken.services.ProductService;

import java.util.Collections;
import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SearchController {

    ProductService productService;

    @GetMapping("/product/suggestions")
    public List<ProductSearchSuggestionDTO> getSuggestions(@RequestParam String query) {
        if (query == null || query.trim().length() < 1)
            return Collections.emptyList();
        return productService.searchByName(query.trim());
    }
}
