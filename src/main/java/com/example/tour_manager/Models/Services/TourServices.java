package com.example.tour_manager.Models.Services;

import com.example.tour_manager.Models.DTO.Rep.TourRes;
import com.example.tour_manager.Models.DTO.Req.TourReq;

import java.util.List;

public interface TourServices {
    List<TourRes> getAll();
    TourRes getById(Long id);
    TourRes create(TourReq request);
    TourRes update(Long id, TourReq request);
    void delete(Long id);
}
