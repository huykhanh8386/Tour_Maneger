package com.example.tour_manager.Models.Services.Impl;

import com.example.tour_manager.Models.Constant.Status;
import com.example.tour_manager.Models.DTO.Rep.BookingRes;
import com.example.tour_manager.Models.DTO.Req.BookingReq;
import com.example.tour_manager.Models.Entities.Booking;
import com.example.tour_manager.Models.Entities.Customer;
import com.example.tour_manager.Models.Entities.TourPackage;
import com.example.tour_manager.Models.Entities.UserAccount;
import com.example.tour_manager.Models.Respositories.BookingRepository;
import com.example.tour_manager.Models.Respositories.CustomerRepository;
import com.example.tour_manager.Models.Respositories.TourRepository;
import com.example.tour_manager.Models.Services.BookingServices;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServicesImpl implements BookingServices {
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final TourRepository tourRepository;
    @Override
    public List<BookingRes> getAll() {
        return bookingRepository.findAll()
                .stream()
                .map(BookingRes::fromEntity)
                .toList();
    }
    @Override
    public List<BookingRes> getMine(String email) {
        return bookingRepository
                .findByCustomer_Account_EmailIgnoreCaseOrderByBookedAtDesc(email)
                .stream()
                .map(BookingRes::fromEntity)
                .toList();
    }
    @Override
    public BookingRes create(String email, BookingReq request) {
        TourPackage tour = tourRepository
                .lockById(request.tourId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour không tồn tại"));
        Customer customer = customerRepository
                .findByAccount_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ khách hàng"));
        if (!tour.getDepartureDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tour đã hoặc đang khởi hành");
        }
        Long sum = bookingRepository.sumBookedSeats(tour.getId(), Status.CONFIRMED);
        long booked = sum == null ? 0 : sum;
        long available = tour.getMaxSeats() - booked;
        if (request.guestCount() > available) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tour không đủ chỗ, hiện còn " + available + " chỗ");
        }
        Booking booking = new Booking();
        booking.setTour(tour);
        booking.setCustomer(customer);
        booking.setGuestCount(request.guestCount());
        booking.setTotalAmount(tour.getPrice().multiply(BigDecimal.valueOf(request.guestCount())));
        booking.setBookedAt(LocalDateTime.now());
        booking.setStatus(Status.CONFIRMED);
        bookingRepository.saveAndFlush(booking);
        return BookingRes.fromEntity(booking);
    }
    @Override
    @Transactional
    public BookingRes cancel(Long bookingId, String email, boolean admin) {
        Long tourId = bookingRepository
                .findTourIdByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Booking"));
        tourRepository.lockById(tourId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour không tồn tại"));
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking không tồn tại"));
        UserAccount owner = booking.getCustomer().getAccount();
        boolean isOwner = owner != null && owner.getEmail().equalsIgnoreCase(email);
        if (!admin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền hủy Booking này");
        }
        if (booking.getStatus() == Status.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking đã được hủy");
        }
        if (!booking.getTour().getDepartureDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể hủy Tour đã hoặc đang khởi hành");
        }
        booking.setStatus(Status.CANCELLED);
        bookingRepository.saveAndFlush(booking);
        return BookingRes.fromEntity(booking);
    }
}