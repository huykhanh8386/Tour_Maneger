package com.example.tour_manager.Models.DTO.Req;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TourReq(
        @NotBlank(message = "Tên tour không được để trống")
        String name,
        @NotBlank(message = "Điểm đến không được để trống")
        String destination,
        @NotBlank(message = "Lịch trình không được để trống")
        String itinerary,
        @NotNull(message = "Ngày khởi hành không được để trống")
        @Future(message = "Ngày khởi hành phải trong tương lai")
        LocalDate departureDate,
        @NotNull(message = "Số ngày không được để trống")
        @Min(value = 1, message = "Số ngày phải lớn hơn 0")
        Integer durationDays,
        @NotNull(message = "Giá không được để trống")
        @DecimalMin(value = "0", inclusive = false, message = "Giá tour phải lớn hơn 0")
        BigDecimal price,
        @NotNull(message = "Số chỗ không được để trống")
        @Min(value = 1, message = "Số chỗ phải lớn hơn 0")
        Integer maxSeats

) {
}
