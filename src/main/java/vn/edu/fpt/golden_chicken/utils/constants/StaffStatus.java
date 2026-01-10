package vn.edu.fpt.golden_chicken.utils.constants;

public enum StaffStatus {
    AVAILABLE, // Đang rảnh, sẵn sàng nhận đơn (dành cho Shipper/Nhân viên nhận đơn)
    BUSY, // Đang làm món hoặc đang đi giao hàng (không thể nhận thêm đơn mới)
    OFFLINE, // Đã hết ca làm việc, không có mặt tại cửa hàng
    BREAK, // Đang nghỉ giải lao giữa ca
    INACTIVE // Nhân viên đã nghỉ việc hoặc bị khóa tài khoản (ngừng hoạt động)
}
