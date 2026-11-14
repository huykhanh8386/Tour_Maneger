package com.example.tour_manager.Controller;

import com.example.tour_manager.Models.DTO.Rep.AuthRes;
import com.example.tour_manager.Models.DTO.Rep.ProfileRes;
import com.example.tour_manager.Models.DTO.Req.LoginReq;
import com.example.tour_manager.Models.DTO.Req.RegisterReq;
import com.example.tour_manager.Models.Services.AuthServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServices authServices;
    @PostMapping("/register")
    public ResponseEntity<AuthRes> register(@Valid @RequestBody RegisterReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServices.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@Valid @RequestBody LoginReq request) {
        return ResponseEntity.ok(authServices.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileRes> profile(Principal principal) {
        return ResponseEntity.ok(authServices.profile(principal.getName()));
    }
}