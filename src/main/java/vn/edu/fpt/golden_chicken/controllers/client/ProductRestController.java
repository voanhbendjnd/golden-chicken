package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.services.ProductService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Autowired
    private ProductService productService;

    @GetMapping("/suggestions")
    public List<Map<String, Object>> getSuggestions(@RequestParam String query) {
        if (query.length() < 1) return Collections.emptyList();

        List<Product> products = productService.searchByName(query);

        // Trả về JSON gọn nhẹ để JS xử lý nhanh
        return products.stream().limit(8).map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            return map;
        }).collect(Collectors.toList());
    }
}
