package com.example.tour_manager.Models.Services;

import com.example.tour_manager.Models.DTO.Rep.CustomerRes;
import com.example.tour_manager.Models.DTO.Rep.RevenueRes;
import com.example.tour_manager.Models.DTO.Rep.TourRes;

import java.util.List;

public interface ReportServices {
    List<CustomerRes> getCustomersByTour(Long tourId);
    List<TourRes> getUpcomingTours();
    RevenueRes getRevenue(Long tourId);
}
