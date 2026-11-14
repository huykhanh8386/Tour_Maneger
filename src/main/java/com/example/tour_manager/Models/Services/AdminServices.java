package com.example.tour_manager.Models.Services;

import com.example.tour_manager.Models.DTO.Rep.AdminRes;
import com.example.tour_manager.Models.DTO.Req.AdminCreateReq;
import com.example.tour_manager.Models.DTO.Req.AdminNameReq;

import java.util.List;

public interface AdminServices {
    List<AdminRes> getAllAdmins();
    AdminRes createAdmin(AdminCreateReq request);
    AdminRes updateAdminName(Long id, AdminNameReq request);
}
