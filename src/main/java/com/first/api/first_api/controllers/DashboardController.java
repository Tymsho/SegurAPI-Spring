package com.first.api.first_api.controllers;

import com.first.api.first_api.dtoresponse.DashboardStatsResponse;
import com.first.api.first_api.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> obtenerStats() {
        DashboardStatsResponse stats = dashboardService.obtenerEstadisticasDashboard();
        return ResponseEntity.ok(stats);
    }
}
