package com.example.tour_manager.Models.DTO.Req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminNameReq(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100)
        String fullName
) {
}
