package com.example.tour_manager.Models.Services.Impl;

import com.example.tour_manager.Models.Constant.Role;

import com.example.tour_manager.Models.DTO.Req.AdminCreateReq;
import com.example.tour_manager.Models.DTO.Req.AdminNameReq;
import com.example.tour_manager.Models.DTO.Rep.AdminRes;

import com.example.tour_manager.Models.Entities.UserAccount;

import com.example.tour_manager.Models.Respositories.UserAccountRepository;
import com.example.tour_manager.Models.Respositories.CustomerRepository;

import com.example.tour_manager.Models.Services.AdminServices;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServicesImpl implements AdminServices {
    private final UserAccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public List<AdminRes> getAllAdmins() {
        return accountRepository.findAll()
                .stream()
                .filter(account -> account.getRole() == Role.ADMIN)
                .map(AdminRes::fromEntity)
                .toList();
    }
    @Override
    public AdminRes createAdmin(AdminCreateReq request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (accountRepository.existsByEmailIgnoreCase(email) || customerRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        UserAccount admin = new UserAccount();
        admin.setFullName(request.fullName().trim());
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setRole(Role.ADMIN);
        accountRepository.saveAndFlush(admin);
        return AdminRes.fromEntity(admin);
    }
    @Override
    public AdminRes updateAdminName(Long id, AdminNameReq request) {
        UserAccount admin = accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản này không phải ADMIN");
        }
        admin.setFullName(request.fullName().trim());
        accountRepository.saveAndFlush(admin);
        return AdminRes.fromEntity(admin);
    }
}