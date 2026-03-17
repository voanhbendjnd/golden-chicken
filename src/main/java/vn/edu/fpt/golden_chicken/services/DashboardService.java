package vn.edu.fpt.golden_chicken.services;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.edu.fpt.golden_chicken.domain.dto.DashboardReportDTO;
import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.repositories.OrderItemRepository;
import vn.edu.fpt.golden_chicken.repositories.OrderRepository;
import vn.edu.fpt.golden_chicken.repositories.ReviewRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;

    public DashboardReportDTO getDashboardData() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
                .atTime(LocalTime.MAX);
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear()).atTime(LocalTime.MAX);

        BigDecimal revenueToday = orderRepository.getTotalRevenueBetween(startOfDay, endOfDay);
        BigDecimal revenueThisMonth = orderRepository.getTotalRevenueBetween(startOfMonth, endOfMonth);
        BigDecimal revenueThisYear = orderRepository.getTotalRevenueBetween(startOfYear, endOfYear);
        long successfulOrdersThisMonth = orderRepository.countSuccessfulOrdersBetween(startOfMonth, endOfMonth);

        DashboardReportDTO.TopProductDTO bestSeller = null;
        List<Object[]> bestSellerData = orderItemRepository.findBestSeller(PageRequest.of(0, 1));
        if (!bestSellerData.isEmpty()) {
            bestSeller = new DashboardReportDTO.TopProductDTO((String) bestSellerData.get(0)[0],
                    ((Number) bestSellerData.get(0)[1]).longValue());
        }

        DashboardReportDTO.TopProductDTO mostReviewed = null;
        List<Object[]> mostReviewedData = reviewRepository.findMostReviewedProduct(PageRequest.of(0, 1));
        if (!mostReviewedData.isEmpty()) {
            mostReviewed = new DashboardReportDTO.TopProductDTO((String) mostReviewedData.get(0)[0],
                    ((Number) mostReviewedData.get(0)[1]).longValue());
        }

        String highestDayStr = "N/A";
        List<Object[]> highDay = orderRepository.getHighestRevenueDay();
        if (!highDay.isEmpty()) {
            highestDayStr = String.format("%s (%s VND)", highDay.get(0)[0], highDay.get(0)[1]);
        }

        String highestMonthStr = "N/A";
        List<Object[]> highMonth = orderRepository.getHighestRevenueMonth();
        if (!highMonth.isEmpty()) {
            highestMonthStr = String.format("Month %s (%s VND)", highMonth.get(0)[0], highMonth.get(0)[1]);
        }

        List<Order> recentOrders = orderRepository.findTop5RecentOrders(PageRequest.of(0, 5));
        List<DashboardReportDTO.RecentOrderDTO> recentOrderDTOs = recentOrders.stream()
                .map(o -> new DashboardReportDTO.RecentOrderDTO(
                        o.getId(),
                        o.getCustomer() != null ? o.getCustomer().getUser().getFullName() : "Guest",
                        o.getFinalAmount(),
                        o.getStatus().name(),
                        o.getCreatedAt().toString()))
                .collect(Collectors.toList());
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> chartRaw = orderRepository.getDailyRevenueLast7Days(sevenDaysAgo);
        List<DashboardReportDTO.DailyRevenueDTO> chartData = chartRaw.stream()
                .map(row -> new DashboardReportDTO.DailyRevenueDTO((String) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());

        return DashboardReportDTO.builder()
                .revenueToday(revenueToday != null ? revenueToday : BigDecimal.ZERO)
                .revenueThisMonth(revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO)
                .revenueThisYear(revenueThisYear != null ? revenueThisYear : BigDecimal.ZERO)
                .successfulOrdersThisMonth(successfulOrdersThisMonth)
                .bestSellerProduct(bestSeller)
                .mostReviewedProduct(mostReviewed)
                .highestRevenueDay(highestDayStr)
                .highestRevenueMonth(highestMonthStr)
                .recentOrders(recentOrderDTOs)
                .last7DaysRevenue(chartData)
                .build();
    }

    public ByteArrayInputStream exportToExcel() throws IOException {
        DashboardReportDTO data = getDashboardData();
        String[] columns = { "Metric", "Value" };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Dashboard Report");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }
            int rowIdx = 1;
            sheet.createRow(rowIdx++).createCell(0).setCellValue("--- CORE METRICS ---");
            addRow(sheet, rowIdx++, "Revenue Today", data.getRevenueToday().toString() + " VND");
            addRow(sheet, rowIdx++, "Revenue This Month", data.getRevenueThisMonth().toString() + " VND");
            addRow(sheet, rowIdx++, "Revenue This Year", data.getRevenueThisYear().toString() + " VND");
            addRow(sheet, rowIdx++, "Successful Orders (Month)", String.valueOf(data.getSuccessfulOrdersThisMonth()));
            sheet.createRow(rowIdx++).createCell(0).setCellValue("--- TOP PERFORMERS ---");
            addRow(sheet, rowIdx++, "Best Seller",
                    data.getBestSellerProduct() != null ? data.getBestSellerProduct().getProductName() : "N/A");
            addRow(sheet, rowIdx++, "Most Reviewed",
                    data.getMostReviewedProduct() != null ? data.getMostReviewedProduct().getProductName() : "N/A");
            addRow(sheet, rowIdx++, "Highest Revenue Day", data.getHighestRevenueDay());
            addRow(sheet, rowIdx++, "Highest Revenue Month", data.getHighestRevenueMonth());
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void addRow(Sheet sheet, int rowIdx, String metric, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(metric);
        row.createCell(1).setCellValue(value);
    }
}
