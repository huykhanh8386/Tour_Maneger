package com.example.tour_manager.Models.Services;

import com.example.tour_manager.Models.DTO.Rep.AuthRes;
import com.example.tour_manager.Models.DTO.Rep.ProfileRes;
import com.example.tour_manager.Models.DTO.Req.LoginReq;
import com.example.tour_manager.Models.DTO.Req.RegisterReq;

public interface AuthServices {
    AuthRes register(RegisterReq request);
    AuthRes login(LoginReq request);
    ProfileRes profile(String email);
}
