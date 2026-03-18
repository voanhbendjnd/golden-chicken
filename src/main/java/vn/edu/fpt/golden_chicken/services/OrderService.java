package vn.edu.fpt.golden_chicken.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.OrderItem;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.ActionPointMessage;
import vn.edu.fpt.golden_chicken.domain.response.OrderMessage;
import vn.edu.fpt.golden_chicken.domain.response.OrderStatisResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResOrder;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.CartRepository;
import vn.edu.fpt.golden_chicken.repositories.CustomerVoucherRepository;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.repositories.VoucherRepository;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;
import vn.edu.fpt.golden_chicken.utils.constants.StatusVoucher;
import vn.edu.fpt.golden_chicken.utils.converts.OrderConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.CheckoutException;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderService {
    @Autowired
    private KafkaTemplate<String, OrderMessage> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, ActionPointMessage> kafkaTemplatePoint;
    UserRepository userRepository;
    ProductRepository productRepository;
    OrderRepository orderRepository;
    CustomerVoucherRepository customerVoucherRepository;
    VoucherRepository voucherRepository;
    CartService cartService;
    UserService userService;
    CartRepository cartRepository;

    @Transactional
    public Order order(OrderDTO dto) throws PermissionException {
        var user = this.userRepository
                .findByEmailIgnoreCase(SecurityContextHolder.getContext().getAuthentication().getName());
        var customer = user.getCustomer();

        if (customer == null) {
            throw new PermissionException("You do not have permission!");
        }
        var orderItems = new ArrayList<OrderItem>();
        var order = new Order();
        order.setName(dto.getName());
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setShippingAddress(dto.getAddress());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setNote(dto.getNote());
        order.setPhone(dto.getPhone());
        order.setStatus(OrderStatus.PENDING);
        order.setCustomer(customer);
        order.setUpdatedAt(LocalDateTime.now());
        var details = this.productRepository
                .findByIdIn(dto.getItems().stream().map(x -> x.getProductId()).collect(Collectors.toList()));
        var mpDetails = details.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));
        BigDecimal lastPriceProduct = BigDecimal.ZERO;
        for (var x : dto.getItems()) {
            var product = mpDetails.get(x.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Product ID", x.getProductId());
            }
            BigDecimal quantity = new BigDecimal(x.getQuantity());
            BigDecimal itemPriceTotal = product.getPrice().multiply(quantity);
            lastPriceProduct = lastPriceProduct.add(itemPriceTotal);
            // product.setSold((product.getSold() != null ? product.getSold() : 0) +
            // x.getQuantity());
            var orderItem = new OrderItem();
            orderItem.setFirstPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItem.setQuantity(x.getQuantity());
            orderItem.setProduct(product);
            orderItems.add(orderItem);
        }
        BigDecimal shippingFee = dto.getShippingFee() != null ? dto.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal calculatedFinalAmount = lastPriceProduct.add(shippingFee).subtract(discount);
        if (calculatedFinalAmount.compareTo(dto.getFinalAmount()) != 0) {
            throw new CheckoutException("Invalid total amount. Recalculated total is " + calculatedFinalAmount);
        }
        order.setTotalProductPrice(lastPriceProduct);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discount);
        order.setProductDiscountAmount(dto.getProductDiscountAmount());
        order.setShippingDiscountAmount(dto.getShippingDiscountAmount());
        order.setFinalAmount(calculatedFinalAmount);
        order.setOrderItems(orderItems);
        var newOrder = this.orderRepository.save(order);
        markVoucherUsed(dto.getProductVoucherId(), newOrder);
        markVoucherUsed(dto.getShippingVoucherId(), newOrder);

        if (dto.getPaymentMethod() == PaymentMethod.COD) {
            OrderMessage message = new OrderMessage();
            message.setCustomerEmail(user.getEmail());
            message.setOrderId(newOrder.getId());
            message.setStatus(newOrder.getStatus());
            message.setTotalPrice(newOrder.getFinalAmount());
            message.setCustomerName(newOrder.getName());
            message.setReason(newOrder.getDeliveryFailedReason());
            this.kafkaTemplate.send("order-chicken-topic", message);
            this.cartService.cleanCartAfterCheckout(dto.getItems());
        }

        return newOrder;
    }

    private void markVoucherUsed(Long voucherId, Order newOrder) {
        if (voucherId == null) {
            return;
        }
        var customerVoucher = customerVoucherRepository.findById(voucherId).orElse(null);
        if (customerVoucher == null || customerVoucher.getVoucher() == null) {
            return;
        }
        customerVoucher.setStatus(StatusVoucher.USED);
        customerVoucher.setUsedAt(LocalDateTime.now());
        customerVoucher.setOrder(newOrder);
        customerVoucherRepository.save(customerVoucher);

        var voucher = customerVoucher.getVoucher();
        int qty = voucher.getQuantity() != null ? voucher.getQuantity() : 0;
        int newQty = qty - 1;
        voucher.setQuantity(newQty);
        if (newQty <= 0) {
            voucher.setStatus("DISABLED");
        }
        voucherRepository.save(voucher);
    }

    public List<OrderDTO.OrderDetail> getItemsDTOByOrderID(Long orderId) throws PermissionException {
        var user = this.userService.getUserInContext();
        var customer = user.getCustomer();
        if (customer == null) {
            throw new PermissionException("You do not have permission!");
        }
        var order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order ID", orderId));
        var orderItems = order.getOrderItems();
        if (orderItems.isEmpty()) {
            return new ArrayList<>();
        }
        var customerId = customer.getId();
        var resList = new ArrayList<OrderDTO.OrderDetail>();
        var cartItems = this.cartRepository.findByCustomerIdAndProductIdIn(customerId,
                orderItems.stream().map(x -> x.getProduct().getId()).toList());
        var mapCartItems = cartItems.stream()
                .collect(Collectors.toMap(x -> x.getProduct().getId(), x -> x.getId(), (c1, c2) -> c1));

        for (var x : orderItems) {
            var productId = x.getProduct().getId();
            var cartItemId = mapCartItems.get(productId);
            if (cartItemId != null) {
                var res = new OrderDTO.OrderDetail();
                res.setItemId(cartItemId);
                res.setProductId(productId);
                res.setQuantity(x.getQuantity());
                resList.add(res);
            }

        }
        return resList;
    }

    public ResultPaginationDTO fetchAllWithPagination(Specification<Order> spec, Pageable pageable) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        var page = this.orderRepository.findAll(spec, pageable);
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(x -> {
            var order = new ResOrder();
            order.setId(x.getId());
            order.setAddress(x.getShippingAddress());
            order.setName(x.getName());
            order.setPhone(x.getPhone());
            order.setNote(x.getNote());
            order.setDeliveryFailedReason(x.getDeliveryFailedReason());
            order.setTotalPrice(x.getFinalAmount());
            order.setPaymentMethod(x.getPaymentMethod().toString());
            order.setPaymentStatus(x.getPaymentStatus().toString());
            order.setStatus(x.getStatus());
            order.setCreatedAt(x.getCreatedAt());
            order.setUpdatedAt(x.getUpdatedAt());
            return order;
        }).collect(Collectors.toList()));
        return res;
    }

    @Transactional
    public void cancelOrderByCustomer(Long id) throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();

        var order = this.orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order ID", id));

        if (order.getCustomer() == null || !order.getCustomer().getUser().getEmail().equals(email)) {
            throw new PermissionException("Bạn không có quyền hủy đơn hàng này!");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Không thể hủy đơn hàng vì đơn đã được xử lý hoặc đã kết thúc!");
        }

        order.setStatus(OrderStatus.CANCELLED);

        if (order.getPaymentStatus().equals(PaymentStatus.PAID)) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        order.setUpdatedAt(LocalDateTime.now());

        var updatedOrder = this.orderRepository.save(order);

        OrderMessage message = new OrderMessage();
        message.setCustomerEmail(email);
        message.setOrderId(updatedOrder.getId());
        message.setStatus(updatedOrder.getStatus());
        message.setTotalPrice(updatedOrder.getFinalAmount());
        message.setCustomerName(updatedOrder.getName());
        message.setReason(updatedOrder.getDeliveryFailedReason());
        this.kafkaTemplate.send("order-chicken-topic", message);
    }

    @Transactional
    public void changeOrderStatus(Long id, String statusName, Staff shipper) {
        this.changeOrderStatus(id, statusName, shipper, null);
    }

    @Transactional
    public void changeOrderStatus(Long id, String statusName, Staff shipper, String reason) {
        var order = this.orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order ID", id));

        if (shipper != null) {
            order.setShipper(shipper);
        }

        OrderStatus currentStatus = order.getStatus();
        PaymentStatus currentPayment = order.getPaymentStatus();
        OrderStatus nextStatus = OrderStatus.safeValueOf(statusName);

        if (nextStatus == null) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + statusName);
        }

        if (currentStatus == nextStatus)
            return;

    
        var actor = this.userService.getUserInContext();
        StaffType actorType = (actor != null && actor.getStaff() != null) ? actor.getStaff().getStaffType() : null;

        boolean isShipperFlowStatus = nextStatus == OrderStatus.DELIVERING
                || nextStatus == OrderStatus.SHIPPER_ISSUE
                || nextStatus == OrderStatus.REASSIGNING_SHIPPER
                || nextStatus == OrderStatus.DELIVERY_FAILED
                || nextStatus == OrderStatus.DELIVERED;

        boolean isLockedForReceptionist = currentStatus == OrderStatus.READY_FOR_DELIVERY
                || currentStatus == OrderStatus.DELIVERING
                || currentStatus == OrderStatus.SHIPPER_ISSUE
                || currentStatus == OrderStatus.REASSIGNING_SHIPPER
                || currentStatus == OrderStatus.DELIVERY_FAILED
                || currentStatus == OrderStatus.DELIVERED
                || currentStatus == OrderStatus.COMPLETED
                || currentStatus == OrderStatus.CANCELLED;

        if (actorType == StaffType.RECEPTIONIST) {
            if (isLockedForReceptionist) {
                throw new RuntimeException("Bạn không có quyền cập nhật trạng thái ở giai đoạn giao hàng.");
            }
            if (isShipperFlowStatus) {
                throw new RuntimeException("Bạn không có quyền cập nhật trạng thái thuộc phạm trù shipper.");
            }
        }

        if (actorType == StaffType.SHIPPER) {
            if (shipper == null) {
                throw new RuntimeException("Shipper không hợp lệ.");
            }
            if (order.getShipper() == null || order.getShipper().getId() == null
                    || !order.getShipper().getId().equals(shipper.getId())) {
                throw new RuntimeException("Bạn không được phép cập nhật đơn hàng không thuộc shipper này.");
            }
        }

        if (currentStatus == OrderStatus.DELIVERED ||
                currentStatus == OrderStatus.CANCELLED ||
                currentStatus == OrderStatus.COMPLETED) {
            throw new RuntimeException("Đơn hàng đã kết thúc, không thể thay đổi trạng thái!");
        }

        order.setStatus(nextStatus);
        order.setUpdatedAt(LocalDateTime.now());

        String normalizedReason = reason == null ? null : reason.trim();
        if (normalizedReason != null && normalizedReason.isEmpty()) {
            normalizedReason = null;
        }

        if (nextStatus == OrderStatus.DELIVERY_FAILED || nextStatus == OrderStatus.SHIPPER_ISSUE) {
            order.setDeliveryFailedReason(normalizedReason);
        } else {
            order.setDeliveryFailedReason(null);
        }

        if (nextStatus == OrderStatus.DELIVERED || nextStatus == OrderStatus.COMPLETED) {
            var actionMessage = new ActionPointMessage();
            actionMessage.setAction(DeclareConstant.action_point_add);
            actionMessage.setReason("ORDER #" + order.getId());
            actionMessage.setChange(
                    order.getTotalProductPrice().divide(new BigDecimal("1000"), 0, RoundingMode.FLOOR).longValue());
            actionMessage.setActionAt(LocalDateTime.now());
            actionMessage.setUserId(order.getCustomer().getId());
            order.setPaymentStatus(PaymentStatus.PAID);
            var items = order.getOrderItems();
            var products = this.productRepository.findByIdIn(items.stream().map(x -> x.getProduct().getId()).toList());
            var mpItems = items.stream().collect(Collectors.toMap(x -> x.getProduct().getId(), x -> x.getQuantity()));
            for (var x : products) {
                var quantity = mpItems.get(x.getId());
                if (quantity != null) {
                    var currentQty = x.getSold() != null ? x.getSold() : 0;
                    x.setSold(currentQty + quantity);
                }
            }
            this.productRepository.saveAll(products);
            this.kafkaTemplatePoint.send("customer-points-topic", actionMessage);
        }
        if (nextStatus == OrderStatus.CANCELLED && currentPayment == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        var lastOrder = this.orderRepository.save(order);

        if (order.getCustomer() != null && order.getCustomer().getUser() != null) {
            OrderMessage message = new OrderMessage();
            message.setCustomerEmail(lastOrder.getCustomer().getUser().getEmail());
            message.setOrderId(lastOrder.getId());
            message.setStatus(lastOrder.getStatus());
            message.setTotalPrice(lastOrder.getFinalAmount());
            message.setCustomerName(lastOrder.getName());
            message.setReason(lastOrder.getDeliveryFailedReason());
            this.kafkaTemplate.send("order-chicken-topic", message);
        }
    }

    public Order getOrderEntity(Long id) {
        return this.orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order ID", id));
    }

    public void updatePaymentStatus(Long orderId, PaymentStatus status) {
        var order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order ID", orderId));
        order.setPaymentStatus(status);
        this.orderRepository.save(order);

        if (status == PaymentStatus.PAID) {
            var user = order.getCustomer().getUser();
            OrderMessage message = new OrderMessage();
            message.setCustomerEmail(user.getEmail());
            message.setOrderId(order.getId());
            message.setStatus(order.getStatus());
            message.setTotalPrice(order.getFinalAmount());
            message.setCustomerName(order.getName());
            message.setReason(order.getDeliveryFailedReason());
            this.kafkaTemplate.send("order-chicken-topic", message);
        }
    }

    @Transactional
    public ResOrder findById(Long id) {
        var order = this.orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order ID", id));
        var res = new ResOrder();
        res.setAddress(order.getShippingAddress());
        res.setCreatedAt(order.getCreatedAt());
        res.setId(order.getId());
        res.setName(order.getName());
        res.setNote(order.getNote());
        res.setDeliveryFailedReason(order.getDeliveryFailedReason());
        res.setPaymentMethod(order.getPaymentMethod().toString());
        res.setPaymentStatus(order.getPaymentStatus().toString());
        res.setPhone(order.getPhone());
        res.setStatus(order.getStatus());
        if (order.getShipper() != null && order.getShipper().getUser() != null) {
            var shipperUser = order.getShipper().getUser();
            res.setShipperName(shipperUser.getFullName());
            res.setShipperPhone(shipperUser.getPhone());
        }
        res.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        res.setTotalPrice(order.getTotalProductPrice());
        res.setFinalAmount(order.getFinalAmount());
        res.setFeeShipping(order.getShippingFee());
        res.setProductDiscountAmount(
                order.getProductDiscountAmount() != null ? order.getProductDiscountAmount() : BigDecimal.ZERO);
        res.setShippingDiscountAmount(
                order.getShippingDiscountAmount() != null ? order.getShippingDiscountAmount() : BigDecimal.ZERO);
        res.setUpdatedAt(order.getUpdatedAt());
        res.setItems(order.getOrderItems().stream().map(x -> {
            var detail = new ResOrder.OrderDetail();
            detail.setId(x.getId());
            detail.setName(x.getProduct().getName());
            detail.setImg(x.getProduct().getImageUrl());
            detail.setProductId(x.getProduct().getId());
            detail.setPrice(x.getProduct().getPrice());
            detail.setQuantity(x.getQuantity());
            return detail;
        }).collect(Collectors.toList()));
        return res;

    }

    public ResultPaginationDTO stateOrder(Specification<Order> spec, Pageable pageable) throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) {
            throw new PermissionException("You Do Not Have Permission!");
        }
        var user = this.userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            throw new PermissionException("Not Found Account With Email " + email);
        }
        var customer = user.getCustomer();
        if (customer == null) {
            throw new PermissionException("Please Use Account Customer For This Service!");
        }
        Specification<Order> orderSpec = (r, q, c) -> {
            return c.equal(r.get("customer"), customer);
        };
        var page = this.orderRepository.findAll(spec.and(orderSpec), pageable);

        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        var result = page.getContent().stream().map(OrderConvert::toResOrder).toList();
        res.setResult(result);
        return res;
    }

    public ResultPaginationDTO getOrderHistory(Specification<Order> spec, Pageable pageable, String statusStr)
            throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) {
            throw new PermissionException("You Do Not Have Permission!");
        }
        var user = this.userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            throw new PermissionException("Not Found Account With Email " + email);
        }
        var customer = user.getCustomer();
        if (customer == null) {
            throw new PermissionException("Please Use Account Customer For This Service!");
        }

        Specification<Order> orderSpec = (r, q, c) -> c.equal(r.get("customer"), customer);

        if (statusStr != null && !statusStr.isEmpty() && !statusStr.equalsIgnoreCase("ALL")) {
            Specification<Order> statusGroupSpec;

            switch (statusStr.toUpperCase()) {
                case "WAITING":
                    statusGroupSpec = (r, q, c) -> r.get("status").in(
                            OrderStatus.PENDING,
                            OrderStatus.PREPARING,
                            OrderStatus.READY_FOR_DELIVERY);
                    break;

                case "IN_PROGRESS":
                    statusGroupSpec = (r, q, c) -> r.get("status").in(
                            OrderStatus.DELIVERING,
                            OrderStatus.SHIPPER_ISSUE,
                            OrderStatus.REASSIGNING_SHIPPER);
                    break;

                case "FINISHED":
                    statusGroupSpec = (r, q, c) -> r.get("status").in(
                            OrderStatus.DELIVERED,
                            OrderStatus.COMPLETED);
                    break;

                case "FAILED":
                    statusGroupSpec = (r, q, c) -> r.get("status").in(
                            OrderStatus.CANCELLED,
                            OrderStatus.DELIVERY_FAILED);
                    break;

                default:
                    try {
                        OrderStatus singleStatus = OrderStatus.valueOf(statusStr);
                        statusGroupSpec = (r, q, c) -> c.equal(r.get("status"), singleStatus);
                    } catch (IllegalArgumentException e) {
                        statusGroupSpec = null;
                    }
                    break;
            }

            if (statusGroupSpec != null) {
                orderSpec = orderSpec.and(statusGroupSpec);
            }
        }

        var page = this.orderRepository.findAll(spec.and(orderSpec), pageable);

        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);

        var result = page.getContent().stream().map(OrderConvert::toResOrder).toList();
        res.setResult(result);

        return res;
    }

    public List<OrderStatisResponse> getOrderStatisticData() {
        String[] monthLabels = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

        List<Object[]> rawData = orderRepository.getMonthlyRevenueRaw();

        Map<Integer, BigDecimal> statsMap = rawData.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (BigDecimal) row[1]));

        List<OrderStatisResponse> revenues = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String label = monthLabels[i - 1];
            BigDecimal total = statsMap.getOrDefault(i, BigDecimal.ZERO);
            revenues.add(new OrderStatisResponse(label, total));
        }

        return revenues;
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        var order = this.orderRepository.findById(orderId).orElse(null);
        if (order == null)
            return;

        if (order.getPaymentStatus() == PaymentStatus.UNPAID
                && order.getStatus() == OrderStatus.PENDING) {
            this.orderRepository.delete(order);
        }
    }
}