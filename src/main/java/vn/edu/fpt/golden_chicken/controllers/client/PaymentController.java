package vn.edu.fpt.golden_chicken.controllers.client;

import java.math.BigDecimal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.services.VNPayService;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Controller
@RequestMapping("/payment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentController {

    VNPayService vnPayService;
    OrderService orderService;

    /**
     * Tạo URL thanh toán VNPay và redirect
     * Gọi sau khi đã tạo đơn hàng thành công (orderId từ CheckoutController)
     */
    @GetMapping("/create")
    public RedirectView createVNPayPayment(
            @RequestParam("orderId") Long orderId,
            HttpServletRequest request) throws PermissionException, ResourceNotFoundException {
        var order = orderService.getOrderEntity(orderId);
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null || order.getCustomer().getUser().getEmail() == null
                || !order.getCustomer().getUser().getEmail().equals(email)) {
            throw new PermissionException("Bạn không có quyền thanh toán đơn hàng này.");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return new RedirectView("/order-history?paid=true");
        }
        String ipAddr = getClientIp(request);
        String orderInfo = "Thanh toan don hang #" + orderId + " - Golden Chicken";
        orderInfo = VNPayService.removeVietnameseAccents(orderInfo);
        String paymentUrl = vnPayService.createPaymentUrl(orderId, order.getFinalAmount(), orderInfo, ipAddr);
        return new RedirectView(paymentUrl);
    }

    /**
     * Callback URL khi VNPay redirect về sau khi thanh toán
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(
            HttpServletRequest request,
            Model model) {
        boolean valid = vnPayService.verifyReturnUrl(request);
        String orderIdStr = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String amountStr = request.getParameter("vnp_Amount");
        String bankCode = request.getParameter("vnp_BankCode");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String payDate = request.getParameter("vnp_PayDate");

        if (!valid) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Chữ ký không hợp lệ. Giao dịch có thể bị giả mạo.");
            return "client/payment/result";
        }

        if (orderIdStr == null || orderIdStr.isEmpty()) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Thiếu thông tin đơn hàng.");
            return "client/payment/result";
        }

        Long orderId;
        try {
            orderId = Long.parseLong(orderIdStr);
        } catch (NumberFormatException e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Mã đơn hàng không hợp lệ.");
            return "client/payment/result";
        }

        if ("00".equals(responseCode)) {
            orderService.updatePaymentStatus(orderId, PaymentStatus.PAID);
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("orderId", orderId);
            model.addAttribute("amount",
                    amountStr != null ? new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)) : null);
            model.addAttribute("bankCode", bankCode);
            model.addAttribute("transactionNo", transactionNo);
            model.addAttribute("payDate", payDate);
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "Thanh toán thất bại hoặc đã bị hủy. Mã lỗi: " + responseCode);
            model.addAttribute("orderId", orderId);
        }
        return "client/payment/payment.success";
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
