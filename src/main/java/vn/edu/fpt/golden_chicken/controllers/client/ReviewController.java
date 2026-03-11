package vn.edu.fpt.golden_chicken.controllers.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.request.ReviewDTO;
import vn.edu.fpt.golden_chicken.repositories.OrderItemRepository;
import vn.edu.fpt.golden_chicken.services.ReviewService;
import vn.edu.fpt.golden_chicken.utils.converts.ProductConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Controller
@RequestMapping("/review")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReviewController {
    ReviewService reviewService;
    OrderItemRepository orderItemRepository;

    @PostMapping("/init")
    public String reviewProductPage(Model model, @RequestParam("orderItemId") Long orderItemId) {
        var orderItem = this.orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new DataInvalidException("Product Not Found!"));
        model.addAttribute("product", ProductConvert.toResProduct(orderItem.getProduct()));
        model.addAttribute("review", new ReviewDTO());
        model.addAttribute("orderItemId", orderItemId);
        return "client/review";
    }

    @PostMapping("/submit")
    public String review(@ModelAttribute("review") ReviewDTO dto, @RequestParam("files") List<MultipartFile> files,
            @RequestParam("orderItemId") Long orderItemId)
            throws IOException, URISyntaxException, PermissionException {
        this.reviewService.reviewOrder(dto, files, orderItemId);
        return "redirect:/home";
    }
}
