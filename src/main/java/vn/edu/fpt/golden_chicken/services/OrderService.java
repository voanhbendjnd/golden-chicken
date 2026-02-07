package vn.edu.fpt.golden_chicken.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.OrderItem;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.exceptions.AmountException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderService {
    UserRepository userRepository;
    ProductRepository productRepository;
    OrderRepository orderRepository;

    public void order(OrderDTO dto) {
        var user = this.userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        var orderItems = new ArrayList<OrderItem>();
        var order = new Order();
        order.setShippingAddress(dto.getAddress());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setNote(dto.getNote());
        order.setPhone(dto.getPhone());

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
            product.setSold(product.getSold() + x.getQuantity());
            totalBonus += product.getPrice().divide(new java.math.BigDecimal(1000)).longValue();
            BigDecimal quantity = new BigDecimal(x.getQuantity());
            lastPriceProduct = lastPriceProduct.add(product.getPrice().multiply(quantity));
            var orderItem = new OrderItem();
            orderItem.setFirstPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItem.setQuantity(x.getQuantity());
            orderItem.setProduct(product);
            orderItems.add(orderItem);
        }
        BigDecimal lastPriceOrder = lastPriceProduct.add(dto.getShippingFee()).subtract(dto.getDiscountAmount());
        if (Math.abs(lastPriceOrder.subtract(dto.getFinalAmount()).intValue()) > 0.01) {
            throw new AmountException("Invalid total amount. Recalculated total is " + lastPriceOrder);
        }
        order.setTotalProductPrice(dto.getTotalProductPrice());
        order.setShippingFee(dto.getShippingFee());
        if (dto.getDiscountAmount() != null) {
            order.setDiscountAmount(dto.getDiscountAmount());
        }
        order.setFinalAmount(dto.getFinalAmount());
        order.setOrderItems(orderItems);
        this.orderRepository.save(order);

    }
}
