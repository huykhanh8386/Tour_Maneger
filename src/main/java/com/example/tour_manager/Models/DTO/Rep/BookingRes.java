package com.example.tour_manager.Models.DTO.Rep;

import com.example.tour_manager.Models.Entities.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingRes(
        Long id,
        Long tourId,
        String tourName,
        LocalDate departureDate,
        Long customerId,
        String customerName,
        Integer guestCount,
        BigDecimal totalAmount,
        LocalDateTime bookedAt,
        String status
) {
    public static BookingRes fromEntity(Booking booking) {
        return new BookingRes(
                booking.getId(),
                booking.getTour().getId(),
                booking.getTour().getName(),
                booking.getTour().getDepartureDate(),
                booking.getCustomer().getId(),
                booking.getCustomer().getFullName(),
                booking.getGuestCount(),
                booking.getTotalAmount(),
                booking.getBookedAt(),
                booking.getStatus().name()
        );
    }
}
