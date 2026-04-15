package com.gallerymart.backend.auth.service;

import com.gallerymart.backend.auth.dto.request.LoginRequest;
import com.gallerymart.backend.auth.dto.request.RegisterRequest;
import com.gallerymart.backend.auth.dto.response.AuthResponse;
import com.gallerymart.backend.auth.dto.response.UserProfileResponse;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.exception.AlreadyExistsException;
import com.gallerymart.backend.exception.InvalidInputException;
import com.gallerymart.backend.exception.ResourceNotFoundException;
import com.gallerymart.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> ALLOWED_ROLES = Set.of("BUYER", "SELLER", "ADMIN");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new AlreadyExistsException("Email is already in use");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .avatarUrl(request.getAvatarUrl())
                .roles(normalizeRoles(request.getRoles()))
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        return buildAuthResponse(user, token);
    }

    public UserProfileResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toProfile(user);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(toProfile(user))
                .build();
    }

    private UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles())
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRoles(String roles) {
        if (roles == null || roles.isBlank()) {
            return "BUYER";
        }

        Set<String> normalizedRoles = Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(role -> role.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        if (normalizedRoles.isEmpty()) {
            return "BUYER";
        }

        boolean hasInvalidRole = normalizedRoles.stream().anyMatch(role -> !ALLOWED_ROLES.contains(role));
        if (hasInvalidRole) {
            throw new InvalidInputException("Invalid role. Allowed roles: BUYER, SELLER, ADMIN");
        }

        return String.join(",", normalizedRoles);
    }
}
