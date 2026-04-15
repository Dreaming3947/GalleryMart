package com.gallerymart.backend.auth.controller;

import com.gallerymart.backend.auth.dto.request.LoginRequest;
import com.gallerymart.backend.auth.dto.request.RegisterRequest;
import com.gallerymart.backend.auth.dto.response.AuthResponse;
import com.gallerymart.backend.auth.dto.response.UserProfileResponse;
import com.gallerymart.backend.auth.service.AuthService;
import com.gallerymart.backend.config.ApiResponse;
import com.gallerymart.backend.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal User currentUser) {
        UserProfileResponse response = authService.getProfileByEmail(currentUser.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
