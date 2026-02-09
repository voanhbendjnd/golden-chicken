package vn.edu.fpt.golden_chicken.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.OrderItem;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResOrder;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;
import vn.edu.fpt.golden_chicken.utils.exceptions.AmountException;
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

    @Transactional
    public void order(OrderDTO dto) throws PermissionException {
        var user = this.userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.getCustomer() == null) {
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
        order.setCustomer(user.getCustomer());
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
            throw new AmountException("Invalid total amount. Recalculated total is " + calculatedFinalAmount);
        }
        user.getCustomer()
                .setPoint(totalBonus + (user.getCustomer().getPoint() != null ? user.getCustomer().getPoint() : 0));
        order.setTotalProductPrice(lastPriceProduct);
        order.setShippingFee(shippingFee);
        if (dto.getDiscountAmount() != null) {
            order.setDiscountAmount(discount);
        }
        order.setFinalAmount(calculatedFinalAmount);
        order.setOrderItems(orderItems);
        var newOrder = this.orderRepository.save(order);

        this.productRepository.saveAll(details);
        this.mailService.allowMailUpdateOrderStatus(user.getEmail(), newOrder.getStatus().toString());
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
                newOrder.getStatus().toString());
    }
}
