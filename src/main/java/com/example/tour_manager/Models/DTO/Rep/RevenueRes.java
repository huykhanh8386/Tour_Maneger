package com.example.tour_manager.Models.DTO.Rep;

import java.math.BigDecimal;

public record RevenueRes(
        Long tourId,
        String tourName,
        long totalBookings,
        long bookedSeats,
        long availableSeats,
        BigDecimal revenue
) {
}
