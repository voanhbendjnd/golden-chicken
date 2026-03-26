package vn.edu.fpt.golden_chicken.controllers.staff;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.StaffRepository;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.services.ShipperLocationStore;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@Controller
@RequestMapping("/staff/shipper")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShipperDashboardController {

    OrderRepository orderRepository;
    StaffRepository staffRepository;
    UserService userService;
    OrderService orderService;
    ShipperLocationStore shipperLocationStore;

    @GetMapping
    public String root() {
        return "redirect:/staff/shipper/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model,
            @RequestParam(name = "range", required = false, defaultValue = "DAY") String range) {
        var user = this.userService.getUserInContext();
        if (user.getStaff() != null) {
            var staffType = user.getStaff().getStaffType();
            if (staffType != StaffType.SHIPPER) {
                if (staffType == StaffType.MANAGER && !user.getRole().getName().equals("ADMIN")) {
                    return "redirect:/staff/dashboard";
                }
                if (staffType == StaffType.RECEPTIONIST) {
                    return "redirect:/staff/order";
                }
            }
        }
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
    public String acceptOrder(@RequestParam("orderId") Long orderId, RedirectAttributes ra) {
        Staff shipper = getCurrentShipper();
        if (shipper == null) {
            return "redirect:/staff";
        }
        if (shipper.getUser() == null || !StringUtils.hasText(shipper.getUser().getPhone())) {
            ra.addFlashAttribute("error", "Bạn cần cập nhật số điện thoại trước khi nhận đơn hàng.");
            return "redirect:/staff/shipper/dashboard";
        }
        Order order = this.orderService.getOrderEntity(orderId);
        boolean isHandoverStatus = order.getStatus() == OrderStatus.SHIPPER_ISSUE
                || order.getStatus() == OrderStatus.REASSIGNING_SHIPPER;

        if (isHandoverStatus && order.getShipper() != null) {
            Long oldShipperId = order.getShipper().getId();
            String oldShipperName = order.getShipper().getUser() != null
                    ? order.getShipper().getUser().getFullName()
                    : null;
            var latestOldLocation = shipperLocationStore.getByOrderId(orderId).orElse(null);
            shipperLocationStore.savePreviousShipperSnapshot(
                    orderId,
                    oldShipperName,
                    latestOldLocation != null ? latestOldLocation.getLat() : null,
                    latestOldLocation != null ? latestOldLocation.getLng() : null,
                    order.getDeliveryFailedReason());
            shipperLocationStore.markReassignedOrderForShipper(oldShipperId, orderId);
        }

        boolean canAccept = (order.getShipper() == null && order.getStatus() == OrderStatus.READY_FOR_DELIVERY)
                || (order.getStatus() == OrderStatus.REASSIGNING_SHIPPER)
                || (order.getStatus() == OrderStatus.SHIPPER_ISSUE);
        if (canAccept) {
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

        var readyOrders = this.orderRepository.findByStatusAndShipperIsNull(OrderStatus.READY_FOR_DELIVERY, pageable);
        var problematicOrders = this.orderRepository.findByStatusAndShipperNot(OrderStatus.SHIPPER_ISSUE, shipper,
                pageable);
        var reassigningOrders = this.orderRepository.findByStatusAndShipperNot(OrderStatus.REASSIGNING_SHIPPER, shipper,
                pageable);
        var newOrdersList = new java.util.ArrayList<>(readyOrders.getContent());
        newOrdersList.addAll(problematicOrders.getContent());
        newOrdersList.addAll(reassigningOrders.getContent());

        var deliveringStatuses = Arrays.asList(OrderStatus.DELIVERING, OrderStatus.SHIPPER_ISSUE,
                OrderStatus.REASSIGNING_SHIPPER);
        var deliveringOrders = this.orderRepository.findByShipperAndStatusIn(shipper, deliveringStatuses, pageable);
        var historyStatuses = Arrays.asList(
                OrderStatus.DELIVERED,
                OrderStatus.DELIVERY_FAILED,
                OrderStatus.CANCELLED,
                OrderStatus.SHIPPER_ISSUE,
                OrderStatus.REASSIGNING_SHIPPER);
        var historyOrders = this.orderRepository.findByShipperAndStatusIn(shipper, historyStatuses, pageable);
        var mergedHistoryOrders = new ArrayList<>(historyOrders.getContent());

        var reassignedOrderIds = shipperLocationStore.getReassignedOrderIdsForShipper(shipper.getId());
        var reassignedReasonMap = new HashMap<Long, String>();
        if (!reassignedOrderIds.isEmpty()) {
            var alreadyInHistory = new HashSet<Long>();
            for (var order : mergedHistoryOrders) {
                alreadyInHistory.add(order.getId());
            }
            var reassignedOrders = this.orderRepository.findAllById(reassignedOrderIds);
            for (var order : reassignedOrders) {
                if (!alreadyInHistory.contains(order.getId())) {
                    mergedHistoryOrders.add(order);
                }
                var snapshot = shipperLocationStore.getPreviousShipperSnapshot(order.getId()).orElse(null);
                if (snapshot != null && snapshot.getIssueReason() != null && !snapshot.getIssueReason().isBlank()) {
                    reassignedReasonMap.put(order.getId(), snapshot.getIssueReason());
                }
            }
            mergedHistoryOrders.sort(Comparator.comparing(Order::getUpdatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        }

        long deliveringCount = this.orderRepository.countByShipperAndStatusIn(shipper, deliveringStatuses);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long deliveredToday = this.orderRepository.countByShipperAndStatusAndUpdatedAtBetween(shipper,
                OrderStatus.DELIVERED, startOfDay, endOfDay);

        long totalDelivered = this.orderRepository.countByShipperAndStatus(shipper, OrderStatus.DELIVERED);
        var shipperIssueStatuses = Arrays.asList(OrderStatus.SHIPPER_ISSUE, OrderStatus.REASSIGNING_SHIPPER);
        long currentIssueCount = this.orderRepository.countByShipperAndStatusIn(shipper, shipperIssueStatuses);
        long shipperIssueCount = currentIssueCount + reassignedOrderIds.size();

        BigDecimal totalCodCollected = this.orderRepository
                .sumFinalAmountByShipperAndPaymentMethodAndPaymentStatus(shipper, PaymentMethod.COD,
                        PaymentStatus.PAID);
        long totalAssigned = this.orderRepository.countByShipper(shipper);

        model.addAttribute("newOrders", newOrdersList);
        model.addAttribute("deliveringOrders", deliveringOrders.getContent());
        model.addAttribute("historyOrders", mergedHistoryOrders);
        model.addAttribute("reassignedOrderIdSet", reassignedOrderIds);
        model.addAttribute("reassignedReasonMap", reassignedReasonMap);
        model.addAttribute("deliveringCount", deliveringCount);
        model.addAttribute("deliveredToday", deliveredToday);
        model.addAttribute("totalDelivered", totalDelivered);
        model.addAttribute("shipperIssueCount", shipperIssueCount);
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

        boolean isReassigning = order.getStatus() == OrderStatus.REASSIGNING_SHIPPER
                || order.getStatus() == OrderStatus.SHIPPER_ISSUE;

        if (order.getShipper() != null
                && !order.getShipper().getId().equals(shipper.getId())
                && !isReassigning) {
            return "redirect:/staff/shipper/dashboard";
        }

        String oldShipperName = null;
        Double oldShipperLat = null;
        Double oldShipperLng = null;

        var snapshot = shipperLocationStore.getPreviousShipperSnapshot(order.getId()).orElse(null);
        if (snapshot != null) {
            oldShipperName = snapshot.getShipperName();
            oldShipperLat = snapshot.getLat();
            oldShipperLng = snapshot.getLng();
        } else if (isReassigning && order.getShipper() != null) {
            if (order.getShipper().getUser() != null) {
                oldShipperName = order.getShipper().getUser().getFullName();
            }
            var oldLocation = shipperLocationStore.getByOrderId(order.getId()).orElse(null);
            oldShipperLat = oldLocation != null ? oldLocation.getLat() : null;
            oldShipperLng = oldLocation != null ? oldLocation.getLng() : null;
        }

        boolean hasOldShipperInfo = oldShipperName != null || (oldShipperLat != null && oldShipperLng != null);

        model.addAttribute("order", orderService.findById(id));
        model.addAttribute("addressOnly", addressOnly);
        model.addAttribute("isReassigning", isReassigning);
        model.addAttribute("hasOldShipperInfo", hasOldShipperInfo);
        model.addAttribute("oldShipperName", oldShipperName);
        model.addAttribute("oldShipperLat", oldShipperLat);
        model.addAttribute("oldShipperLng", oldShipperLng);

        return "staff/order/detail-fragment :: orderDetail";
    }

}
