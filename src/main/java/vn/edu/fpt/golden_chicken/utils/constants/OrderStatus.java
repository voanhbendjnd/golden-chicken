package vn.edu.fpt.golden_chicken.utils.constants;

public enum OrderStatus {
    PENDING,
    PREPARING,
    READY_FOR_DELIVERY,
    DELIVERING,
    DELIVERED,

    SHIPPER_ISSUE,
    REASSIGNING_SHIPPER,
    DELIVERY_FAILED,

    CANCELLED,
    COMPLETED;

    public static OrderStatus safeValueOf(String status) {
        if (status == null)
            return null;
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }
}