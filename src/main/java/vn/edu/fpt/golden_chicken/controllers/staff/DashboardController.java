package vn.edu.fpt.golden_chicken.controllers.staff;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.edu.fpt.golden_chicken.domain.request.DashboardReportDTO;
import vn.edu.fpt.golden_chicken.services.DashboardService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RequiredArgsConstructor
@Controller("staffDashboardController")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/staff/dashboard")
    public String getDashboard(Model model) {
        DashboardReportDTO report = dashboardService.getDashboardData();
        model.addAttribute("report", report);
        return "staff/dashboard";
    }

    @GetMapping("/staff/dashboard/export")
    public ResponseEntity<InputStreamResource> exportDashboard() throws IOException {
        ByteArrayInputStream in = dashboardService.exportToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=sales_report.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
