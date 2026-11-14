package com.example.tour_manager.Models.DTO.Rep;

import com.example.tour_manager.Models.Entities.Customer;

public record CustomerRes(
        Long id,
        String fullName,
        String email,
        String phone
) {
    public static CustomerRes fromEntity(Customer customer) {
        return new CustomerRes(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone()
        );
    }
}
