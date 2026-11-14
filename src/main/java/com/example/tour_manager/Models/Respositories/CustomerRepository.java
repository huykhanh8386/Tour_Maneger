package com.example.tour_manager.Models.Respositories;

import com.example.tour_manager.Models.Entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<Customer> findByAccount_EmailIgnoreCase(String email);
}
