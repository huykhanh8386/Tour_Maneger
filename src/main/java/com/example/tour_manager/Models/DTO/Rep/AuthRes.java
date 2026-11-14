package com.example.tour_manager.Models.DTO.Rep;

import com.example.tour_manager.Models.Entities.TourPackage;

public record AuthRes(
        String accessToken,
        String tokenType,
        String role,
        String email
) {
    public static TourRes fromEntity(
            TourPackage tour,
            long bookedSeats
    ) {
        return new TourRes(
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
