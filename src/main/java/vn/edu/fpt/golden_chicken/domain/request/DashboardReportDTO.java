package vn.edu.fpt.golden_chicken.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportDTO {
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueThisYear;
    private Long successfulOrdersThisMonth;

    private TopProductDTO bestSellerProduct;
    private TopProductDTO mostReviewedProduct;
    private String highestRevenueDay;
    private String highestRevenueMonth;
    private List<RecentOrderDTO> recentOrders;

    private List<DailyRevenueDTO> last7DaysRevenue;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopProductDTO {
        private String productName;
        private Long value;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyRevenueDTO {
        private String date;
        private BigDecimal revenue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentOrderDTO {
        private Long orderId;
        private String customerName;
        private BigDecimal amount;
        private String status;
        private String date;
    }
}
