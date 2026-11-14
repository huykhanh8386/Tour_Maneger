package com.example.tour_manager.Models.Respositories;

import com.example.tour_manager.Models.Constant.Status;
import com.example.tour_manager.Models.Entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            SELECT COALESCE(SUM(b.guestCount), 0)
            FROM Booking b
            WHERE b.tour.id = :tourId
              AND b.status = :status
            """)
    Long sumBookedSeats(@Param("tourId") Long tourId, @Param("status") Status status);
    @Query("""
            SELECT b.tour.id
            FROM Booking b
            WHERE b.id = :id
            """)
    Optional<Long> findTourIdByBookingId(@Param("id") Long bookingId);
    List<Booking> findByTour_IdAndStatus(Long tourId, Status status);
    List<Booking> findByCustomer_Account_EmailIgnoreCaseOrderByBookedAtDesc(String email);
    long countByTour_IdAndStatus(Long tourId, Status status);
    boolean existsByTour_Id(Long tourId);
    boolean existsByCustomer_Id(Long customerId);
}
