package vn.edu.fpt.golden_chicken.controllers.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import vn.edu.fpt.golden_chicken.domain.response.*;
import vn.edu.fpt.golden_chicken.services.*;

import java.util.*;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalHeaderAdvice {

    private final CategoryService categoryService;
    private final ProductService productService;

    @ModelAttribute
    public void addMenuNav(Model model) {

        List<ResCategory> categories = categoryService.fetchAll();
        List<ResProduct> products = productService.getAllActiveForMenu();

        if (categories == null)
            categories = List.of();
        if (products == null)
            products = List.of();

        Map<String, List<ResProduct>> map = new LinkedHashMap<>();

        for (ResProduct p : products) {
            if (p == null || p.getCategory() == null)
                continue;
            String name = p.getCategory().getName();
            if (name == null || name.isBlank())
                continue;

            map.computeIfAbsent(name.trim(), k -> new ArrayList<>()).add(p);
        }

        List<MenuCategoryNav> menuCategories = new ArrayList<>();

        for (ResCategory c : categories) {
            if (c == null || c.getName() == null)
                continue;

            List<ResProduct> list = map.get(c.getName());
            if (list == null || list.isEmpty())
                continue;

            String anchor = slug(c.getName());
            String img = list.get(0).getImg();

            if (img == null)
                img = "dish.svg";

            menuCategories.add(new MenuCategoryNav(
                    c.getName(),
                    img,
                    anchor));
        }

        model.addAttribute("menuCategories", menuCategories);
    }

    private String slug(String input) {
        if (input == null)
            return "";

        String s = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        s = s.replace("(", "")
                .replace(")", "");

        s = s.toLowerCase()
                .replace("&", " ")
                .replaceAll("[^a-z0-9\\s-]", " ")
                .trim()
                .replaceAll("\\s+", "-");

        return s;
    }
}