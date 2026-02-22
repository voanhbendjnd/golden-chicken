package vn.edu.fpt.golden_chicken.utils.constants;

public enum OrderStatus {
    PENDING, SHIPPING, COMPLETED, CANCELLED, DELIVERED;

    public static OrderStatus safeValueOf(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }
}
