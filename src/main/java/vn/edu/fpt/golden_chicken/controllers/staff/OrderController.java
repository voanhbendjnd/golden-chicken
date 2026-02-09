package vn.edu.fpt.golden_chicken.controllers.staff;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.turkraft.springfilter.boot.Filter;

import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.services.OrderService;

@Controller
@RequestMapping("/staff/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String table(Model model, @Filter Specification<Order> spec,
            @PageableDefault(size = DefineVariable.pageSize, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var data = this.orderService.fetchAllWithPagination(spec, pageable);
        model.addAttribute("meta", data.getMeta());
        model.addAttribute("orders", data.getResult());
        return "staff/order/table";
    }

    @PostMapping("/update-status/{id:[0-9]+}")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@RequestParam String status, @PathVariable("id") Long id) {
        this.orderService.changeOrderStatus(id, status);
        return ResponseEntity.ok().build();
    }

}
