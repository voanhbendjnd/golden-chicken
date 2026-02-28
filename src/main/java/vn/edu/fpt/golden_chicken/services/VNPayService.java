package vn.edu.fpt.golden_chicken.services;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.config.VNPayConfig;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VNPayService {

    VNPayConfig vnPayConfig;

    /**
     * Tạo URL thanh toán VNPay
     *
     * @param orderId   Mã đơn hàng (vnp_TxnRef)
     * @param amount    Số tiền thanh toán (VND)
     * @param orderInfo Mô tả đơn hàng (không dấu, không ký tự đặc biệt)
     * @param ipAddr    IP khách hàng
     * @return URL redirect đến VNPay
     */
    public String createPaymentUrl(Long orderId, BigDecimal amount, String orderInfo, String ipAddr) {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).longValue() + "");
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId.toString());
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddr);
        vnpParams.put("vnp_CreateDate", java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(java.time.LocalDateTime.now()));

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII));
                query.append("=");
                query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
                query.append("&");
            }
        }
        String queryString = query.toString();
        if (queryString.endsWith("&")) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }

        String secureHash = hmacSHA512(vnPayConfig.getHashSecret(), queryString);
        return vnPayConfig.getPayUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Xác thực checksum từ VNPay callback
     */
    public boolean verifyReturnUrl(HttpServletRequest request) {
        Map<String, String> fields = new TreeMap<>();
        for (var e : request.getParameterMap().entrySet()) {
            String key = e.getKey();
            if (key.startsWith("vnp_") && !"vnp_SecureHash".equals(key) && !"vnp_SecureHashType".equals(key)) {
                fields.put(key, e.getValue()[0]);
            }
        }
        String signValue = request.getParameter("vnp_SecureHash");
        if (signValue == null || signValue.isEmpty()) {
            return false;
        }
        String hashData = fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        return calculatedHash.equalsIgnoreCase(signValue);
    }

    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("VNPay HMAC error", e);
        }
    }

    /**
     * Chuyển chuỗi tiếng Việt có dấu sang không dấu (cho vnp_OrderInfo)
     */
    public static String removeVietnameseAccents(String str) {
        if (str == null)
            return "";
        String[] vietnamese = { "à", "á", "ạ", "ả", "ã", "â", "ầ", "ấ", "ậ", "ẩ", "ẫ", "ă", "ằ", "ắ", "ặ", "ẳ", "ẵ",
                "è", "é", "ẹ", "ẻ", "ẽ", "ê", "ề", "ế", "ệ", "ể", "ễ", "ì", "í", "ị", "ỉ", "ĩ", "ò", "ó", "ọ", "ỏ", "õ",
                "ô", "ồ", "ố", "ộ", "ổ", "ỗ", "ơ", "ờ", "ớ", "ợ", "ở", "ỡ", "ù", "ú", "ụ", "ủ", "ũ", "ư", "ừ", "ứ", "ự",
                "ử", "ữ", "ỳ", "ý", "ỵ", "ỷ", "ỹ", "đ", "À", "Á", "Ạ", "Ả", "Ã", "Â", "Ầ", "Ấ", "Ậ", "Ẩ", "Ẫ", "Ă",
                "Ằ", "Ắ", "Ặ", "Ẳ", "Ẵ", "È", "É", "Ẹ", "Ẻ", "Ẽ", "Ê", "Ề", "Ế", "Ệ", "Ể", "Ễ", "Ì", "Í", "Ị", "Ỉ",
                "Ĩ", "Ò", "Ó", "Ọ", "Ỏ", "Õ", "Ô", "Ồ", "Ố", "Ộ", "Ổ", "Ỗ", "Ơ", "Ờ", "Ớ", "Ợ", "Ở", "Ỡ", "Ù", "Ú",
                "Ụ", "Ủ", "Ũ", "Ư", "Ừ", "Ứ", "Ự", "Ử", "Ữ", "Ỳ", "Ý", "Ỵ", "Ỷ", "Ỹ", "Đ" };
        String[] ascii = { "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "e",
                "e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "i", "i", "i", "i", "i", "o", "o", "o", "o", "o",
                "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "u", "u", "u", "u", "u", "u", "u", "u",
                "u", "u", "u", "y", "y", "y", "y", "y", "d", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A",
                "A", "A", "A", "A", "A", "A", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "I", "I", "I",
                "I", "I", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "U",
                "U", "U", "U", "U", "U", "U", "U", "U", "U", "U", "Y", "Y", "Y", "Y", "Y", "D" };
        for (int i = 0; i < vietnamese.length; i++) {
            str = str.replace(vietnamese[i], ascii[i]);
        }
        return str.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }
}
