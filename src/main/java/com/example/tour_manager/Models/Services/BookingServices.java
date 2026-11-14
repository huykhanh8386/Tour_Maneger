package com.example.tour_manager.Models.Services;

import com.example.tour_manager.Models.DTO.Rep.BookingRes;
import com.example.tour_manager.Models.DTO.Req.BookingReq;

import java.util.List;

public interface BookingServices {
    List<BookingRes> getAll();
    List<BookingRes> getMine(String email);
    BookingRes create(String email, BookingReq request);
    BookingRes cancel(Long bookingId, String email, boolean admin);
}
