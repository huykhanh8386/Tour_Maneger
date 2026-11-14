package com.example.tour_manager.Models.Services.Impl;

import com.example.tour_manager.Models.Constant.Role;
import com.example.tour_manager.Models.DTO.Rep.AuthRes;
import com.example.tour_manager.Models.DTO.Rep.ProfileRes;
import com.example.tour_manager.Models.DTO.Req.LoginReq;
import com.example.tour_manager.Models.DTO.Req.RegisterReq;
import com.example.tour_manager.Models.Entities.Customer;
import com.example.tour_manager.Models.Entities.UserAccount;
import com.example.tour_manager.Models.Respositories.CustomerRepository;
import com.example.tour_manager.Models.Respositories.UserAccountRepository;
import com.example.tour_manager.Models.Services.AuthServices;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthServicesImpl implements AuthServices {
    private final UserAccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
    @Override
    public AuthRes register(RegisterReq request) {
        String email = normalizeEmail(request.email());
        if (accountRepository.existsByEmailIgnoreCase(email) || customerRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setRole(Role.CUSTOMER);
        accountRepository.save(account);
        Customer customer = new Customer();
        customer.setFullName(request.fullName().trim());
        customer.setEmail(email);
        customer.setPhone(request.phone().trim());
        customer.setAccount(account);
        customerRepository.save(customer);
        return generateToken(account);
    }
    @Override
    public AuthRes login(LoginReq request) {
        UserAccount account = accountRepository
                .findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác"));
        if (!passwordEncoder.matches(
                request.password(),
                account.getPassword()
        )) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác");
        }
        return generateToken(account);
    }
    @Override
    public ProfileRes profile(String email) {
        UserAccount account = accountRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        String fullName = customerRepository
                .findByAccount_EmailIgnoreCase(email)
                .map(Customer::getFullName)
                .orElseGet(() -> {
                    String adminName = account.getFullName();
                    return adminName != null && !adminName.isBlank() ? adminName : "Quản trị viên";});
        return new ProfileRes(
                fullName,
                account.getEmail(),
                account.getRole().name()
        );
    }

    private AuthRes generateToken(UserAccount account) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tour-manager")
                .subject(account.getEmail())
                .issuedAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .claim("roles", List.of("ROLE_" + account.getRole().name())).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthRes(
                token,
                "Bearer",
                account.getRole().name(),
                account.getEmail()
        );
    }
}

