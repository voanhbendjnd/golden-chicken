package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.turkraft.springfilter.boot.Filter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDetailController {
    OrderService orderService;

    // @GetMapping("/order-history")
    // public String viewStateOrder(Model model, @Filter Specification<Order> spec,
    // @PageableDefault(size = DefineVariable.pageSize, sort = "updatedAt",
    // direction = Sort.Direction.DESC) Pageable pageable)
    // throws PermissionException {
    // var data = this.orderService.stateOrder(spec, pageable);
    // model.addAttribute("data", data);
    // return "client/order-history";
    // }

    @GetMapping("/order-history")
    public String getOrderHistory(@RequestParam(required = false) OrderStatus status, Model model,
            @Filter Specification<Order> spec,
            @PageableDefault(size = DefineVariable.pageSize, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable)
            throws PermissionException {
        var data = this.orderService.getOrderHistory(spec, pageable, status);
        model.addAttribute("data", data);
        return "client/order-history";
    }

    @GetMapping("/order/{id:[0-9]+}")
    public String getOrderDetail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("order", this.orderService.findById(id));
        return "client/order.detail";
    }
}
