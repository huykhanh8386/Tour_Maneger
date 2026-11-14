package com.example.tour_manager.Models.Services.Impl;

import com.example.tour_manager.Models.Constant.Status;
import com.example.tour_manager.Models.DTO.Rep.TourRes;
import com.example.tour_manager.Models.DTO.Req.TourReq;
import com.example.tour_manager.Models.Entities.TourPackage;
import com.example.tour_manager.Models.Respositories.BookingRepository;
import com.example.tour_manager.Models.Respositories.TourRepository;
import com.example.tour_manager.Models.Services.TourServices;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TourServicesImpl implements TourServices {
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private TourPackage findTour(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour không tồn tại"));
    }
    private long bookedSeats(Long id) {
        Long count = bookingRepository.sumBookedSeats(id, Status.CONFIRMED);
        return count == null ? 0L : count;
    }
    private TourRes response(TourPackage tour) {
        return TourRes.fromEntity(tour, bookedSeats(tour.getId()));
    }
    private void copy(TourPackage tour, TourReq req) {
        tour.setName(req.name().trim());
        tour.setDestination(req.destination().trim());
        tour.setItinerary(req.itinerary().trim());
        tour.setDepartureDate(req.departureDate());
        tour.setDurationDays(req.durationDays());
        tour.setPrice(req.price());
        tour.setMaxSeats(req.maxSeats());
    }
    @Override
    public List<TourRes> getAll() {
        return tourRepository.findAll()
                .stream()
                .map(this::response)
                .toList();
    }
    @Override
    public TourRes getById(Long id) {
        return response(findTour(id));
    }
    @Override
    public TourRes create(TourReq request) {
        TourPackage tour = new TourPackage();
        copy(tour, request);
        tourRepository.saveAndFlush(tour);
        return response(tour);
    }
    @Override
    @Transactional
    public TourRes update(Long id, TourReq request) {
        TourPackage tour = tourRepository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour không tồn tại"));
        long occupied = bookedSeats(id);
        if (request.maxSeats() < occupied) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số chỗ tối đa nhỏ hơn số chỗ đã đặt");
        }
        copy(tour, request);
        tourRepository.saveAndFlush(tour);
        return response(tour);
    }
    @Override
    public void delete(Long id) {
        TourPackage tour = tourRepository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour không tồn tại"));
        if (bookingRepository.existsByTour_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tour đã có lịch sử đặt, không thể xóa");
        }
        tourRepository.delete(tour);
    }
}
