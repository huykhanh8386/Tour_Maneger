package com.example.tour_manager.Controller;

import com.example.tour_manager.Models.DTO.Req.CustomerReq;
import com.example.tour_manager.Models.DTO.Rep.CustomerRes;

import com.example.tour_manager.Models.Services.CustomerServices;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {
    private final CustomerServices customerServices;
    @GetMapping
    public ResponseEntity<List<CustomerRes>> getAll() {
        return ResponseEntity.ok(customerServices.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<CustomerRes> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerServices.getById(id));
    }
    @PostMapping
    public ResponseEntity<CustomerRes> create(@Valid @RequestBody CustomerReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerServices.create(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<CustomerRes> update(@PathVariable Long id, @Valid @RequestBody CustomerReq request) {
        return ResponseEntity.ok(customerServices.update(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerServices.delete(id);
        return ResponseEntity.noContent().build();
    }
}
