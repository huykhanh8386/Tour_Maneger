package com.example.tour_manager.Models.Services.Impl;

import com.example.tour_manager.Models.Constant.Status;
import com.example.tour_manager.Models.DTO.Rep.CustomerRes;
import com.example.tour_manager.Models.DTO.Rep.RevenueRes;
import com.example.tour_manager.Models.DTO.Rep.TourRes;
import com.example.tour_manager.Models.Entities.Booking;
import com.example.tour_manager.Models.Entities.TourPackage;
import com.example.tour_manager.Models.Respositories.BookingRepository;
import com.example.tour_manager.Models.Respositories.TourRepository;
import com.example.tour_manager.Models.Services.ReportServices;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServicesImpl implements ReportServices {
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private TourPackage findTour(Long id) {
        return tourRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour không tồn tại"));
    }
    private long bookedSeats(Long tourId) {
        Long sum = bookingRepository.sumBookedSeats(tourId, Status.CONFIRMED);
        return sum == null ? 0L : sum;
    }
    @Override
    public List<CustomerRes> getCustomersByTour(Long tourId) {
        findTour(tourId);
        return bookingRepository
                .findByTour_IdAndStatus(tourId, Status.CONFIRMED)
                .stream()
                .map(Booking::getCustomer)
                .map(CustomerRes::fromEntity)
                .distinct()
                .toList();
    }
    @Override
    public List<TourRes> getUpcomingTours() {
        return tourRepository
                .findByDepartureDateGreaterThanOrderByDepartureDateAsc(LocalDate.now())
                .stream()
                .map(tour -> TourRes.fromEntity(tour, bookedSeats(tour.getId()))).toList();
    }

    @Override
    public RevenueRes getRevenue(Long tourId) {
        TourPackage tour = findTour(tourId);
        List<Booking> bookings = bookingRepository
                .findByTour_IdAndStatus(tourId, Status.CONFIRMED);
        BigDecimal revenue = bookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long booked = bookedSeats(tourId);
        return new RevenueRes(
                tourId,
                tour.getName(),
                bookings.size(),
                booked,
                tour.getMaxSeats() - booked,
                revenue
        );
    }
}
