package vn.edu.fpt.golden_chicken.controllers.client;

import com.turkraft.springfilter.boot.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.response.CategoryProductsView;
import vn.edu.fpt.golden_chicken.domain.response.MenuCategoryNav;
import vn.edu.fpt.golden_chicken.domain.response.ResCategory;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.services.CategoryService;
import vn.edu.fpt.golden_chicken.services.ProductService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/menu")
public class MenuController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/draw")
    public String getMenuPage(Model model,
            @RequestParam(name = "cat", required = false) String cat,
            @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {

        List<ResCategory> categories = categoryService.fetchAll();
        List<ResProduct> products = productService.getAllActiveForMenu();

        if (categories == null)
            categories = List.of();
        if (products == null)
            products = List.of();

        Map<String, List<ResProduct>> productsByCatName = new LinkedHashMap<>();
        for (ResProduct p : products) {
            if (p == null || p.getCategory() == null)
                continue;
            String catName = p.getCategory().getName();
            if (catName == null || catName.isBlank())
                continue;

            productsByCatName.computeIfAbsent(catName.trim(), k -> new ArrayList<>()).add(p);
        }

        List<CategoryProductsView> categoryProductList = new ArrayList<>();
        List<MenuCategoryNav> menuCategories = new ArrayList<>();
        Map<String, String> categoryAnchorMap = new LinkedHashMap<>();

        for (ResCategory c : categories) {
            if (c == null || c.getName() == null || c.getName().isBlank())
                continue;

            String catName = c.getName().trim();
            String anchor = slug(catName);

            if (cat != null && !cat.isBlank() && !anchor.equals(cat)) {
                continue;
            }

            List<ResProduct> list = productsByCatName.getOrDefault(catName, List.of());
            if (list.isEmpty())
                continue;

            categoryProductList.add(new CategoryProductsView(catName, list));

            categoryAnchorMap.put(catName, anchor);

            String thumbFile = pickThumbFromProducts(list); // filename (DB)
            menuCategories.add(new MenuCategoryNav(catName, thumbFile, anchor));
        }

        if (cat != null && !cat.isBlank()) {
        }

        model.addAttribute("menuCategories", menuCategories);
        model.addAttribute("categoryAnchorMap", categoryAnchorMap);
        model.addAttribute("categoryProductList", categoryProductList);
        model.addAttribute("products", products);
        model.addAttribute("selectedCat", cat);

        return "client/menu";
    }

    @GetMapping
    public String getMenuPageOptimal(Model model,
            @RequestParam(name = "cat", required = false) String cat,
            @RequestParam(name = "q", required = false) String q,
            @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {

        List<String> categoriesName = categoryService.fetchAll().stream().map(x -> x.getName()).toList();
        List<ResProduct> products = productService.getAllActiveForMenu();

        if (categoriesName == null)
            categoriesName = List.of();
        if (products == null)
            products = List.of();

        final String rawQuery = q == null ? "" : q.trim();
        final String normalizedQuery = normalizeForSearch(rawQuery);
        if (!normalizedQuery.isBlank()) {
            products = products.stream()
                    .filter(p -> p != null && p.getName() != null
                            && normalizeForSearch(p.getName()).contains(normalizedQuery))
                    .toList();
        }
        Map<String, List<ResProduct>> productsByCatName = products.stream()
                .filter(p -> p != null && p.getCategory() != null && p.getCategory().getName() != null)
                .collect(Collectors.groupingBy(p -> p.getCategory().getName().trim(), LinkedHashMap::new,
                        Collectors.toList()));

        List<CategoryProductsView> categoryProductList = new ArrayList<>();
        List<MenuCategoryNav> menuCategories = new ArrayList<>();
        Map<String, String> categoryAnchorMap = new LinkedHashMap<>();

        for (var cateName : categoriesName) {
            if (cateName.isBlank())
                continue;

            String anchor = slug(cateName);

            if (cat != null && !cat.isBlank() && !anchor.equals(cat)) {
                continue;
            }

            List<ResProduct> list = productsByCatName.getOrDefault(cateName, List.of());
            if (list.isEmpty())
                continue;

            categoryProductList.add(new CategoryProductsView(cateName, list));

            categoryAnchorMap.put(cateName, anchor);

            String thumbFile = pickThumbFromProducts(list);
            menuCategories.add(new MenuCategoryNav(cateName, thumbFile, anchor));
        }
        if (cat != null && !cat.isBlank()) {
            menuCategories = buildFullNav(categoriesName, productsByCatName);
        }
        // model.addAttribute("menuCategories", menuCategories);
        model.addAttribute("categoryAnchorMap", categoryAnchorMap);
        model.addAttribute("categoryProductList", categoryProductList);
        model.addAttribute("products", products);
        model.addAttribute("selectedCat", cat);
        model.addAttribute("searchQuery", rawQuery);
        return "client/menu";
    }

    private List<MenuCategoryNav> buildFullNav(List<String> categories,
            Map<String, List<ResProduct>> productsByCatName) {

        List<MenuCategoryNav> nav = new ArrayList<>();

        for (var c : categories) {
            if (c.isBlank())
                continue;

            String catName = c.trim();
            List<ResProduct> list = productsByCatName.getOrDefault(catName, List.of());
            if (list.isEmpty())
                continue;

            String anchor = slug(catName);
            String thumbFile = pickThumbFromProducts(list);

            nav.add(new MenuCategoryNav(catName, thumbFile, anchor));
        }
        return nav;
    }

    private String pickThumbFromProducts(List<ResProduct> list) {
        if (list == null || list.isEmpty())
            return null;

        for (ResProduct p : list) {
            if (p == null)
                continue;

            if (hasText(p.getImg()))
                return p.getImg();

            if (p.getImgs() != null && !p.getImgs().isEmpty()
                    && hasText(p.getImgs().get(0))) {
                return p.getImgs().get(0);
            }
        }
        return null;
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private String slug(String input) {
        if (input == null)
            return "";
        String s = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.toLowerCase()
                .replace("&", " ")
                .replaceAll("[^a-z0-9\\s-]", " ")
                .trim()
                .replaceAll("\\s+", "-");
        return s;
    }

    private String normalizeForSearch(String input) {
        if (input == null)
            return "";
        String s = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return s.toLowerCase().trim();
    }

    @GetMapping("/ga-gion")
    public String getGaGon(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllChickenHappy(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Gà Giòn");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }

    // ========== Djnd contribute adding more features to the product filter.
    @GetMapping("/ga-sot")
    public String getGaSot(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllChickenSauce(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Gà Sốt");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }

    @GetMapping("/combo")
    public String getCombo(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllComboWithPagination(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Combo");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }

    @GetMapping("/my")
    public String getMy(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllNoodle(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Mỳ");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }

    @GetMapping("/trang-mieng")
    public String getTrangMieng(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllLowMeal(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Tráng Miệng");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }
}