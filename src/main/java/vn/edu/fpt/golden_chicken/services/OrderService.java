package vn.edu.fpt.golden_chicken.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.CartItem;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.OrderItem;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.OrderStatisResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResOrder;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.CartRepository;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;
import vn.edu.fpt.golden_chicken.utils.converts.OrderConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.CheckoutException;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderService {
    UserRepository userRepository;
    ProductRepository productRepository;
    OrderRepository orderRepository;
    MailService mailService;
    CartRepository cartRepository;

    @Transactional
    public Order order(OrderDTO dto) throws PermissionException {
        var user = this.userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
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
        var details = this.productRepository
                .findByIdIn(dto.getItems().stream().map(x -> x.getProductId()).collect(Collectors.toList()));
        var mpDetails = details.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));
        long totalBonus = 0L;
        BigDecimal lastPriceProduct = BigDecimal.ZERO;
        for (var x : dto.getItems()) {
            var product = mpDetails.get(x.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Product ID", x.getProductId());
            }
            BigDecimal quantity = new BigDecimal(x.getQuantity());
            BigDecimal itemPriceTotal = product.getPrice().multiply(quantity);
            lastPriceProduct = lastPriceProduct.add(itemPriceTotal);
            product.setSold(product.getSold() != null ? product.getSold() : 0 + x.getQuantity());
            totalBonus += itemPriceTotal.divide(new BigDecimal("1000"), 0, RoundingMode.FLOOR).longValue();
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
        customer
                .setPoint(totalBonus + (customer.getPoint() != null ? customer.getPoint() : 0));
        order.setTotalProductPrice(lastPriceProduct);
        order.setShippingFee(shippingFee);
        if (dto.getDiscountAmount() != null) {
            order.setDiscountAmount(discount);
        }
        order.setFinalAmount(calculatedFinalAmount);
        order.setOrderItems(orderItems);
        var newOrder = this.orderRepository.save(order);
        var listFromCart = new ArrayList<CartItem>();
        if (dto.getItems().getFirst().getItemId() != null) {
            var cartItems = this.cartRepository
                    .findByIdIn(dto.getItems().stream().map(x -> x.getItemId()).collect(Collectors.toList()));
            var mpCartItems = dto.getItems().stream()
                    .collect(Collectors.toMap(x -> x.getItemId(), x -> x));
            for (var x : cartItems) {
                var qtyInCart = x.getQuantity();
                if (qtyInCart <= 0) {
                    throw new CheckoutException("Quantity Product with Name (" + x.getProduct().getName()
                            + ") in cart less than or equal 0!");
                }
                var it = mpCartItems.get(x.getId());
                if (it != null) {
                    var lastQty = qtyInCart - it.getQuantity();
                    if (lastQty < 0) {
                        throw new CheckoutException("Quantity Product with Name (" + x.getProduct().getName()
                                + ") in cart less than or equal 0!");

                    } else if (lastQty == 0) {
                        listFromCart.add(x);
                    } else if (lastQty > 0) {
                        x.setQuantity(lastQty);
                    }

                } else {
                    throw new ResourceNotFoundException("Cart Item ID", x.getId());
                }
            }
            this.cartRepository.saveAll(cartItems);
            if (!listFromCart.isEmpty() || listFromCart.size() != 0) {
                this.cartRepository.deleteAll(listFromCart);
            }
        }

        this.productRepository.saveAll(details);
        this.mailService.allowMailUpdateOrderStatus(user.getEmail(), newOrder.getStatus().toString(),
                "#" + order.getId(), order.getName());

        return newOrder;
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

    public void changeOrderStatus(Long id, String status) {
        var order = this.orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order ID", id));
        switch (status) {
            case "PENDING":
                order.setStatus(OrderStatus.PENDING);
                break;
            case "SHIPPING":
                order.setStatus(OrderStatus.SHIPPING);
                break;
            case "COMPLETED":
                order.setStatus(OrderStatus.COMPLETED);
                break;
            case "CANCELLED":
                order.setStatus(OrderStatus.CANCELLED);
                break;
            case "DELIVERED":
                order.setStatus(OrderStatus.DELIVERED);
                break;
            default:
                break;
        }

        var newOrder = this.orderRepository.save(order);
        this.mailService.allowMailUpdateOrderStatus(order.getCustomer().getUser().getEmail(),
                newOrder.getStatus().toString(), "#" + order.getId(), order.getName());
    }

    public ResOrder findById(Long id) {
        var order = this.orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order ID", id));
        var res = new ResOrder();
        res.setAddress(order.getShippingAddress());
        res.setCreatedAt(order.getCreatedAt());
        res.setId(order.getId());
        res.setName(order.getName());
        res.setNote(order.getNote());
        res.setPaymentMethod(order.getPaymentMethod().toString());
        res.setPaymentStatus(order.getPaymentStatus().toString());
        res.setPhone(order.getPhone());
        res.setStatus(order.getStatus());
        res.setTotalPrice(order.getTotalProductPrice());
        res.setUpdatedAt(order.getUpdatedAt());
        res.setItems(order.getOrderItems().stream().map(x -> {
            var detail = new ResOrder.OrderDetail();
            detail.setId(x.getId());
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
        var user = this.userRepository.findByEmail(email);
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
        // if (page.getContent().isEmpty() || page.getContent().size() == 0) {
        // throw new DataInvalidException("No Data!");
        // }
        // res.setResult(page.getContent().stream().map(order -> {
        // var resOrder = new ResOrder();
        // resOrder.setAddress(order.getShippingAddress());
        // resOrder.setCreatedAt(order.getCreatedAt());
        // resOrder.setId(order.getId());
        // resOrder.setName(order.getName());
        // resOrder.setNote(order.getNote());
        // resOrder.setPaymentMethod(order.getPaymentMethod().toString());
        // resOrder.setPaymentStatus(order.getPaymentStatus().toString());
        // resOrder.setPhone(order.getPhone());
        // resOrder.setStatus(order.getStatus());
        // resOrder.setTotalPrice(order.getTotalProductPrice());
        // resOrder.setUpdatedAt(order.getUpdatedAt());
        // resOrder.setItems(order.getOrderItems().stream().filter(x -> x.getProduct()
        // != null).map(x -> {
        // var detail = new ResOrder.OrderDetail();
        // detail.setId(x.getId());
        // var product = x.getProduct();
        // detail.setImg(product.getImageUrl());
        // detail.setProductId(product.getId());
        // detail.setPrice(product.getPrice());
        // detail.setQuantity(x.getQuantity());
        // return detail;
        // }).toList());
        // return resOrder;
        // }).toList());
        return res;
    }

    public ResultPaginationDTO getOrderHistory(Specification<Order> spec, Pageable pageable, OrderStatus status)
            throws PermissionException {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) {
            throw new PermissionException("You Do Not Have Permission!");
        }
        var user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new PermissionException("Not Found Account With Email " + email);
        }
        var customer = user.getCustomer();
        if (customer == null) {
            throw new PermissionException("Please Use Account Customer For This Service!");
        }
        // if (status != OrderStatus.CANCELLED || status != OrderStatus.COMPLETED ||
        // status != OrderStatus.PENDING
        // || status != OrderStatus.SHIPPING) {

        // }
        Specification<Order> orderSpec = (r, q, c) -> {
            return c.equal(r.get("customer"), customer);
        };
        // var lastStatus = status.toString();
        if (status != null) {
            var checkStatus = OrderStatus.safeValueOf(status.toString());
            if (checkStatus != null) {
                Specification<Order> orderStatus = (r, q, c) -> {
                    return c.equal(r.get("status"), status);
                };
                orderSpec = orderSpec.and(orderStatus);
                // var page = this.orderRepository.findAll(spec.and(orderSpec).and(orderStatus),
                // pageable);
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
        // Mảng tên tháng để hiển thị lên biểu đồ
        String[] monthLabels = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

        // 1. Gọi Repository lấy dữ liệu đã được SUM và GROUP BY dưới DB
        List<Object[]> rawData = orderRepository.getMonthlyRevenueRaw();

        // 2. Chuyển List<Object[]> thành Map<Integer, BigDecimal>
        // Key là tháng (1-12), Value là tổng tiền
        Map<Integer, BigDecimal> statsMap = rawData.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0], // Tháng (kết quả của MONTH())
                        row -> (BigDecimal) row[1] // Tổng doanh thu (kết quả của SUM())
                ));

        // 3. Khởi tạo danh sách kết quả đủ 12 tháng (điền 0 nếu tháng đó không có doanh
        // thu)
        List<OrderStatisResponse> revenues = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String label = monthLabels[i - 1];
            BigDecimal total = statsMap.getOrDefault(i, BigDecimal.ZERO);
            revenues.add(new OrderStatisResponse(label, total));
        }

        return revenues;
    }
}
