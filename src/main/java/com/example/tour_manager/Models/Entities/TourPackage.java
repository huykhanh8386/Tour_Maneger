package com.example.tour_manager.Models.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tour_packages")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TourPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false, length = 150)
    private String destination;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String itinerary;
    @Column(nullable = false)
    private LocalDate departureDate;
    @Column(nullable = false)
    private Integer durationDays;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer maxSeats;
}