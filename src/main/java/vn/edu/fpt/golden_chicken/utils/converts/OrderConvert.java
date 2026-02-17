package vn.edu.fpt.golden_chicken.utils.converts;

import java.util.Objects;

import org.springframework.stereotype.Component;

import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.OrderItem;
import vn.edu.fpt.golden_chicken.domain.response.ResOrder;

@Component
public class OrderConvert {
    public static ResOrder toResOrder(Order order) {
        var resOrder = new ResOrder();
        resOrder.setAddress(order.getShippingAddress());
        resOrder.setCreatedAt(order.getCreatedAt());
        resOrder.setId(order.getId());
        resOrder.setName(order.getName());
        resOrder.setNote(order.getNote());
        resOrder.setPaymentMethod(order.getPaymentMethod().toString());
        resOrder.setPaymentStatus(order.getPaymentStatus().toString());
        resOrder.setPhone(order.getPhone());
        resOrder.setStatus(order.getStatus());
        resOrder.setTotalPrice(order.getTotalProductPrice());
        resOrder.setUpdatedAt(order.getUpdatedAt());
        resOrder.setItems(order.getOrderItems().stream().map(OrderConvert::toDetail).filter(Objects::nonNull).toList());
        return resOrder;
    }

    private static ResOrder.OrderDetail toDetail(OrderItem item) {
        var product = item.getProduct();
        var order = item.getOrder();
        if (product == null || order == null) {
            return null;
        }
        var res = new ResOrder.OrderDetail();

        res.setId(item.getId());
        res.setImg(product.getImageUrl());
        res.setName(order.getName());
        res.setPrice(product.getPrice());
        res.setProductId(product.getId());
        res.setQuantity(item.getQuantity());
        return res;
    }
}
