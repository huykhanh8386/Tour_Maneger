package com.example.tour_manager.Controller;

import com.example.tour_manager.Models.DTO.Rep.TourRes;
import com.example.tour_manager.Models.DTO.Req.TourReq;
import com.example.tour_manager.Models.Services.TourServices;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TourController {
    private final TourServices tourServices;
    @GetMapping
    public ResponseEntity<List<TourRes>> getAll() {
        return ResponseEntity.ok(tourServices.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<TourRes> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tourServices.getById(id));
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourRes> create(@Valid @RequestBody TourReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourServices.create(request));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourRes> update(@PathVariable Long id, @Valid @RequestBody TourReq request) {
        return ResponseEntity.ok(tourServices.update(id, request));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourServices.delete(id);
        return ResponseEntity.noContent().build();
    }
}