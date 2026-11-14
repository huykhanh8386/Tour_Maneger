package com.example.tour_manager.Models.DTO.Rep;

import com.example.tour_manager.Models.Entities.TourPackage;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TourRes(
        Long id,
        String name,
        String destination,
        String itinerary,
        LocalDate departureDate,
        Integer durationDays,
        BigDecimal price,
        Integer maxSeats,
        long bookedSeats,
        long availableSeats
) {
    public static TourRes fromEntity(TourPackage tour, long bookedSeats) {
        return new TourRes
                (
                        tour.getId(),
                        tour.getName(),
                        tour.getDestination(),
                        tour.getItinerary(),
                        tour.getDepartureDate(),
                        tour.getDurationDays(),
                        tour.getPrice(),
                        tour.getMaxSeats(),
                        bookedSeats,
                        tour.getMaxSeats() - bookedSeats
                );
    }
}
