package com.example.tour_manager.Models.Services;

import com.example.tour_manager.Models.DTO.Rep.CustomerRes;
import com.example.tour_manager.Models.DTO.Req.CustomerReq;

import java.util.List;

public interface CustomerServices {
    List<CustomerRes> getAll();
    CustomerRes getById(Long id);
    CustomerRes create(CustomerReq request);
    CustomerRes update(Long id, CustomerReq request);
    void delete(Long id);
}
