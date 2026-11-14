package com.example.tour_manager.Models.Respositories;

import com.example.tour_manager.Models.Entities.TourPackage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TourRepository extends JpaRepository<TourPackage, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TourPackage t WHERE t.id = :id")
    Optional<TourPackage> lockById(@Param("id") Long id);
    List<TourPackage> findByDepartureDateGreaterThanOrderByDepartureDateAsc(LocalDate date);
}
