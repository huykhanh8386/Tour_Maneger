package com.example.tour_manager.Controller;

import com.example.tour_manager.Models.DTO.Req.BookingReq;
import com.example.tour_manager.Models.DTO.Rep.BookingRes;

import com.example.tour_manager.Models.Services.BookingServices;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BookingController {
    private final BookingServices bookingServices;
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingRes>> getAll() {
        return ResponseEntity.ok(bookingServices.getAll());
    }
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingRes>> getMine(Authentication authentication) {
        return ResponseEntity.ok(bookingServices.getMine(authentication.getName()));
    }
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingRes> create(@Valid @RequestBody BookingReq request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingServices.create(authentication.getName(), request));
    }
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingRes> cancel(@PathVariable Long id, Authentication authentication) {
        boolean admin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(bookingServices.cancel(id, authentication.getName(), admin));
    }
}