package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.services.VNPayService;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;

    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params, Model model) {
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
        String orderIdStr = params.get("vnp_TxnRef");

        // Verify payment
        boolean isValid = vnPayService.verifyPayment(params);

        if (isValid && "00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
            // Payment successful
            try {
                Long orderId = Long.parseLong(orderIdStr);
                var order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    order.setPaymentStatus(PaymentStatus.PAID);
                    orderRepository.save(order);
                    model.addAttribute("success", true);
                    model.addAttribute("message", "Thanh toán thành công!");
                    model.addAttribute("orderId", orderId);
                } else {
                    model.addAttribute("success", false);
                    model.addAttribute("message", "Không tìm thấy đơn hàng!");
                }
            } catch (NumberFormatException e) {
                model.addAttribute("success", false);
                model.addAttribute("message", "Mã đơn hàng không hợp lệ!");
            }
        } else {
            // Payment failed
            model.addAttribute("success", false);
            if (!isValid) {
                model.addAttribute("message", "Xác thực thanh toán thất bại!");
            } else {
                model.addAttribute("message", "Thanh toán thất bại! Mã lỗi: " + vnp_ResponseCode);
            }
        }

        return "client/payment/result";
    }
}
