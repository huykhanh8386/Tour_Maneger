package com.example.tour_manager.Controller;

import com.example.tour_manager.Models.DTO.Rep.CustomerRes;
import com.example.tour_manager.Models.DTO.Rep.TourRes;
import com.example.tour_manager.Models.DTO.Rep.RevenueRes;

import com.example.tour_manager.Models.Services.ReportServices;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final ReportServices reportServices;
    @GetMapping("/tours/{id}/customers")
    public ResponseEntity<List<CustomerRes>> customers(@PathVariable Long id) {
        return ResponseEntity.ok(reportServices.getCustomersByTour(id));
    }
    @GetMapping("/upcoming")
    public ResponseEntity<List<TourRes>> upcoming() {
        return ResponseEntity.ok(reportServices.getUpcomingTours());
    }
    @GetMapping("/tours/{id}/revenue")
    public ResponseEntity<RevenueRes> revenue(@PathVariable Long id) {
        return ResponseEntity.ok(reportServices.getRevenue(id));
    }
}