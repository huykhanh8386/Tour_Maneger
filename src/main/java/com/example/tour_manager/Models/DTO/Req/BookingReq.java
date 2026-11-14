package com.example.tour_manager.Models.DTO.Req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingReq(
        @NotNull(message = "Tour không được để trống")
        @Positive(message = "ID tour không hợp lệ")
        Long tourId,
        @NotNull(message = "Số người không được để trống")
        @Min(value = 1, message = "Phải đặt ít nhất 1 người")
        Integer guestCount
) {
}
