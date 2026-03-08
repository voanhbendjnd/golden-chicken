package vn.edu.fpt.golden_chicken.utils.constants;

public enum OrderStatus {
    // Luồng cơ bản
    PENDING,                // Chờ xác nhận
    PREPARING,              // Đang chuẩn bị hàng
    READY_FOR_DELIVERY,     // Đã đóng gói, chờ shipper lấy hàng
    DELIVERING,             // Đang trong quá trình giao hàng
    DELIVERED,              // Giao hàng thành công

    // Luồng xử lý sự cố & Giao lại
    SHIPPER_ISSUE,          // Shipper gặp trục trặc (xe hỏng, tai nạn...)
    REASSIGNING_SHIPPER,    // Đang điều phối shipper khác
    DELIVERY_FAILED,        // Giao hàng thất bại (khách không nghe máy, sai địa chỉ)

    // Luồng kết thúc khác
    CANCELLED,              // Đơn hàng bị hủy (bởi khách hoặc shop)
    COMPLETED;              // Đơn hàng đã hoàn tất (sau khi khách nhận & không khiếu nại)

    public static OrderStatus safeValueOf(String status) {
        if (status == null) return null;
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }
}