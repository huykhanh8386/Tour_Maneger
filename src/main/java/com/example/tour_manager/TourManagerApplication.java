package com.example.tour_manager;

import com.example.tour_manager.Models.Constant.Role;
import com.example.tour_manager.Models.Entities.UserAccount;
import com.example.tour_manager.Models.Respositories.UserAccountRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Tour Manager API", version = "1.0", description = "Hệ thống Quản lý Tour Du lịch"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class TourManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TourManagerApplication.class, args);
    }
    @Bean
    ApplicationRunner initAdmin(
            UserAccountRepository repository,
            PasswordEncoder encoder,
            @Value("${app.admin.name}") String fullName,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password
    ) {
        return args -> {
            String adminEmail = email.trim().toLowerCase();
            if (repository.existsByEmailIgnoreCase(adminEmail)) {
                return;
            }
            UserAccount admin = new UserAccount();
            admin.setFullName(fullName.trim());
            admin.setEmail(adminEmail);
            admin.setPassword(encoder.encode(password));
            admin.setRole(Role.ADMIN);
            repository.save(admin);
            System.out.println("Đã tạo ADMIN ban đầu: " + adminEmail);
        };
    }
}
