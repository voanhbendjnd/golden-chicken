package vn.edu.fpt.golden_chicken.controllers.staff;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.StaffRepository;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;

@Controller
@RequestMapping("/staff/shipper")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShipperDashboardController {

    OrderRepository orderRepository;
    StaffRepository staffRepository;
    UserService userService;
    OrderService orderService;

    @GetMapping
    public String root() {
        return "redirect:/staff/shipper/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model,
            @RequestParam(name = "range", required = false, defaultValue = "DAY") String range) {
        Staff shipper = getCurrentShipper();
        if (shipper == null) {
            return "redirect:/staff";
        }

        loadCommonData(model, shipper);
        model.addAttribute("range", range);

        return "staff/dashboardshiper";
    }

    @GetMapping("/orders")
    public String ordersPage(Model model) {
        Staff shipper = getCurrentShipper();
        if (shipper == null) {
            return "redirect:/staff";
        }
        loadCommonData(model, shipper);
        return "staff/shipper-orders";
    }

    @GetMapping("/statistics")
    public String statisticsPage(Model model) {
        Staff shipper = getCurrentShipper();
        if (shipper == null) {
            return "redirect:/staff";
        }
        loadCommonData(model, shipper);
        return "staff/shipper-statistics";
    }

    @PostMapping("/order/accept")
    public String acceptOrder(@RequestParam("orderId") Long orderId) {
        Staff shipper = getCurrentShipper();
        if (shipper == null) {
            return "redirect:/staff";
        }
        Order order = this.orderService.getOrderEntity(orderId);
        if (order.getShipper() == null && order.getStatus() == OrderStatus.READY_FOR_DELIVERY) {
            order.setShipper(shipper);

            this.orderService.changeOrderStatus(orderId, OrderStatus.DELIVERING.name(), shipper);

        }
        return "redirect:/staff/shipper/dashboard";
    }

    @PostMapping("/order/update-status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
            @RequestParam("status") String status,
            @RequestParam(name = "reason", required = false) String reason) {
        Staff shipper = getCurrentShipper();
        if (shipper == null) {
            return "redirect:/staff";
        }
        Order order = this.orderService.getOrderEntity(orderId);
        if (order.getShipper() != null && order.getShipper().getId().equals(shipper.getId())) {
            this.orderService.changeOrderStatus(orderId, status, shipper, reason);
        }
        return "redirect:/staff/shipper/dashboard";
    }

    private void loadCommonData(Model model, Staff shipper) {
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "updatedAt"));

        var newOrders = this.orderRepository.findByStatusAndShipperIsNull(OrderStatus.READY_FOR_DELIVERY, pageable);

        var deliveringOrders = this.orderRepository.findByShipperAndStatus(shipper, OrderStatus.DELIVERING, pageable);
        var historyStatuses = Arrays.asList(OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED, OrderStatus.CANCELLED);
        var historyOrders = this.orderRepository.findByShipperAndStatusIn(shipper, historyStatuses, pageable);

        long deliveringCount = this.orderRepository.countByShipperAndStatus(shipper, OrderStatus.DELIVERING);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long deliveredToday = this.orderRepository.countByShipperAndStatusAndUpdatedAtBetween(shipper,
                OrderStatus.DELIVERED, startOfDay, endOfDay);

        long totalDelivered = this.orderRepository.countByShipperAndStatus(shipper, OrderStatus.DELIVERED);

        BigDecimal totalCodCollected = this.orderRepository
                .sumFinalAmountByShipperAndPaymentMethodAndPaymentStatus(shipper, PaymentMethod.COD,
                        PaymentStatus.PAID);
        long totalAssigned = this.orderRepository.countByShipper(shipper);

        model.addAttribute("newOrders", newOrders.getContent());
        model.addAttribute("deliveringOrders", deliveringOrders.getContent());
        model.addAttribute("historyOrders", historyOrders.getContent());
        model.addAttribute("deliveringCount", deliveringCount);
        model.addAttribute("deliveredToday", deliveredToday);
        model.addAttribute("totalDelivered", totalDelivered);
        model.addAttribute("totalAssigned", totalAssigned);
        model.addAttribute("totalCodCollected", totalCodCollected);
    }

    private Staff getCurrentShipper() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        var user = this.userService.getByEmail(email);
        if (user == null || user.getStaff() == null) {
            return null;
        }
        return this.staffRepository.findById(user.getStaff().getId()).orElse(null);
    }

   @GetMapping("/order/fragment/{id}")
    public String orderDetail(@PathVariable("id") Long id,
        @RequestParam(name = "addressOnly", defaultValue = "false") boolean addressOnly,
        Model model) {

        Staff shipper = getCurrentShipper();
        if (shipper == null) {
            return "redirect:/staff";
        }

        Order order = orderService.getOrderEntity(id);

        // bảo mật: shipper chỉ xem đơn của mình
         if (order.getShipper() != null
                 && !order.getShipper().getId().equals(shipper.getId())) {
             return "redirect:/staff/shipper/dashboard";
         }

        model.addAttribute("order", orderService.findById(id));
        model.addAttribute("addressOnly", addressOnly);
        
       return "staff/order/detail-fragment :: orderDetail"; 
    }

}
