package vn.edu.fpt.golden_chicken;
import java.util.ArrayList;
import java.util.List;

public class UpdateStatusTest {
    // Giả lập Database đơn giản cho Golden Chicken
    static class Order {
        int id;
        String status;
        Order(int id, String status) { this.id = id; this.status = status; }
    }

//    public static void main(String[] args) {
//        System.out.println("======= SYSTEM TEST: UPDATE ORDER STATUS =======");
//
//        // Khởi tạo dữ liệu mẫu (Pre-conditions)
//        List<Order> db = new ArrayList<>();
//        db.add(new Order(1, "PENDING"));   // Cho TC09
//        db.add(new Order(2, "DELIVERED")); // Cho TC11
//
//        // --- Thực thi TC09: Update PENDING -> CONFIRMED ---
//        System.out.println("\n[Running TC09]: Update status to Confirmed");
//        boolean res09 = performUpdate(db.get(0), "CONFIRMED");
//        System.out.println("Result: " + (res09 && db.get(0).status.equals("CONFIRMED") ? "PASS" : "FAIL"));
//
//        // --- Thực thi TC11: Chặn DELIVERED -> PENDING ---
//        System.out.println("\n[Running TC11]: Prevent Reverting Delivered Order");
//        boolean res11 = performUpdate(db.get(1), "PENDING");
//        System.out.println("Result: " + (!res11 ? "PASS (System blocked invalid change)" : "FAIL (Bug!)"));
//
//        System.out.println("\n================ TEST COMPLETED ================");
//    }
//
//    // Logic nghiệp vụ (Business Logic) giống trong Service của bạn
//    public static boolean performUpdate(Order order, String newStatus) {
//        System.out.println("Action: Changing Order #" + order.id + " from " + order.status + " to " + newStatus);
//
//        // Quy tắc: Nếu đã giao (DELIVERED) thì không được đổi nữa
//        if (order.status.equals("DELIVERED")) {
//            System.out.println("Error: Cannot change status of a delivered order!");
//            return false;
//        }
//
//        order.status = newStatus;
//        return true;
//    }
}
