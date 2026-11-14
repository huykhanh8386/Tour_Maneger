package com.example.tour_manager.Controller;

import com.example.tour_manager.Models.DTO.Req.AdminCreateReq;
import com.example.tour_manager.Models.DTO.Req.AdminNameReq;
import com.example.tour_manager.Models.DTO.Rep.AdminRes;

import com.example.tour_manager.Models.Services.AdminServices;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminServices adminServices;
    @GetMapping
    public ResponseEntity<List<AdminRes>> getAllAdmins() {
        return ResponseEntity.ok(adminServices.getAllAdmins());
    }
    @PostMapping
    public ResponseEntity<AdminRes> createAdmin(@Valid @RequestBody AdminCreateReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminServices.createAdmin(request));
    }
    @PutMapping("/{id}/name")
    public ResponseEntity<AdminRes> updateName(@PathVariable Long id, @Valid @RequestBody AdminNameReq request) {
        return ResponseEntity.ok(adminServices.updateAdminName(id, request));
    }
}
