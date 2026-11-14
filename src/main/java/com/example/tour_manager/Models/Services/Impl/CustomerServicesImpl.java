package com.example.tour_manager.Models.Services.Impl;

import com.example.tour_manager.Models.DTO.Rep.CustomerRes;
import com.example.tour_manager.Models.DTO.Req.CustomerReq;
import com.example.tour_manager.Models.Entities.Customer;
import com.example.tour_manager.Models.Entities.UserAccount;
import com.example.tour_manager.Models.Respositories.BookingRepository;
import com.example.tour_manager.Models.Respositories.CustomerRepository;
import com.example.tour_manager.Models.Respositories.UserAccountRepository;
import com.example.tour_manager.Models.Services.CustomerServices;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServicesImpl implements CustomerServices {
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final UserAccountRepository accountRepository;
    private Customer findCustomer(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại"));
    }
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
    @Override
    public List<CustomerRes> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerRes::fromEntity)
                .toList();
    }
    @Override
    public CustomerRes getById(Long id) {
        return CustomerRes.fromEntity(findCustomer(id));
    }
    @Override
    public CustomerRes create(CustomerReq request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(email) || accountRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }
        Customer customer = new Customer();
        customer.setFullName(request.fullName().trim());
        customer.setEmail(email);
        customer.setPhone(request.phone().trim());
        customerRepository.saveAndFlush(customer);
        return CustomerRes.fromEntity(customer);
    }
    @Override
    public CustomerRes update(Long id, CustomerReq request) {
        Customer customer = findCustomer(id);
        String email = normalizeEmail(request.email());
        if (!customer.getEmail().equalsIgnoreCase(email)) {
            if (customerRepository.existsByEmailIgnoreCase(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
            }
            UserAccount linkedAccount = customer.getAccount();
            boolean usedByAnotherAccount = accountRepository
                    .findByEmailIgnoreCase(email)
                    .filter(other -> linkedAccount == null || !other.getId().equals(linkedAccount.getId())).isPresent();
            if (usedByAnotherAccount) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
            }
            if (linkedAccount != null) {
                linkedAccount.setEmail(email);
            }
        }
        customer.setFullName(request.fullName().trim());
        customer.setEmail(email);
        customer.setPhone(request.phone().trim());
        customerRepository.saveAndFlush(customer);
        return CustomerRes.fromEntity(customer);
    }
    @Override
    public void delete(Long id) {
        Customer customer = findCustomer(id);
        if (bookingRepository.existsByCustomer_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Khách hàng đã đặt Tour, không thể xóa");
        }
        UserAccount account = customer.getAccount();
        customerRepository.delete(customer);
        customerRepository.flush();
        if (account != null) {
            accountRepository.delete(account);
        }
    }
}