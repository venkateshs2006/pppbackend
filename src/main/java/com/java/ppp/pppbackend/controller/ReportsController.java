package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.ApiResponse;
import com.java.ppp.pppbackend.dto.ReportsDashboardDTO;
import com.java.ppp.pppbackend.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ReportsDashboardDTO>> getDashboardData(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "all") String project) {

        ReportsDashboardDTO data = reportsService.getDashboardData(period, project);

        return ResponseEntity.ok(ApiResponse.<ReportsDashboardDTO>builder()
                .success(true)
                .message("Reports data fetched successfully")
                .data(data)
                .build());
    }
}