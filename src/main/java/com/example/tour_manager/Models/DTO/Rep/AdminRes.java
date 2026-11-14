package com.example.tour_manager.Models.DTO.Rep;

import com.example.tour_manager.Models.Entities.UserAccount;

public record AdminRes(
        Long id,
        String fullName,
        String email,
        String role
) {
    public static AdminRes fromEntity(UserAccount account) {
        return new AdminRes(
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getRole().name()
        );
    }
}
