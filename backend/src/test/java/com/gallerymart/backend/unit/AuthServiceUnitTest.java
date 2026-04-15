package com.gallerymart.backend.unit;

import com.gallerymart.backend.auth.dto.request.LoginRequest;
import com.gallerymart.backend.auth.dto.request.RegisterRequest;
import com.gallerymart.backend.auth.dto.request.UpdateProfileRequest;
import com.gallerymart.backend.auth.dto.response.AuthResponse;
import com.gallerymart.backend.auth.dto.response.UserProfileResponse;
import com.gallerymart.backend.auth.service.AuthService;
import com.gallerymart.backend.auth.service.JwtService;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.exception.InvalidInputException;
import com.gallerymart.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void should_register_user_with_default_buyer_role_when_roles_not_provided() {
        RegisterRequest request = RegisterRequest.builder()
                .email("  UnitAuth@Example.com ")
                .password("Password123")
                .fullName(" Unit Auth ")
                .avatarUrl("https://img.test/u.png")
                .build();

        when(userRepository.existsByEmail("unitauth@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(604800000L);

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("unitauth@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRoles()).isEqualTo("BUYER");
        assertThat(savedUser.getFullName()).isEqualTo("Unit Auth");

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("unitauth@example.com");
        assertThat(response.getUser().getRoles()).isEqualTo("BUYER");
    }

    @Test
    void should_throw_invalid_input_when_register_with_invalid_role() {
        RegisterRequest request = RegisterRequest.builder()
                .email("role@test.com")
                .password("Password123")
                .fullName("Role Test")
                .roles("BUYER,HACKER")
                .build();

        when(userRepository.existsByEmail("role@test.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    void should_login_and_return_token_when_credentials_are_valid() {
        LoginRequest request = LoginRequest.builder()
                .email("login@test.com")
                .password("Password123")
                .build();

        User principal = User.builder()
                .id(2L)
                .email("login@test.com")
                .password("encoded")
                .fullName("Login User")
                .roles("BUYER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn((UserDetails) principal);
        when(userRepository.findByEmail("login@test.com")).thenReturn(Optional.of(principal));
        when(jwtService.generateToken(principal)).thenReturn("jwt-login-token");
        when(jwtService.getExpirationTime()).thenReturn(604800000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-login-token");
        assertThat(response.getUser().getId()).isEqualTo(2L);
        assertThat(response.getUser().getEmail()).isEqualTo("login@test.com");
    }

        @Test
        void should_update_profile_for_current_user() {
                User existingUser = User.builder()
                                .id(10L)
                                .email("profile@test.com")
                                .fullName("Old Name")
                                .avatarUrl(null)
                                .roles("BUYER")
                                .password("encoded")
                                .build();

                UpdateProfileRequest request = UpdateProfileRequest.builder()
                                .fullName("  New Name  ")
                                .avatarUrl(" https://img.test/new.png ")
                                .build();

                when(userRepository.findByEmail("profile@test.com")).thenReturn(Optional.of(existingUser));
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                UserProfileResponse response = authService.updateProfile("profile@test.com", request);

                assertThat(response.getFullName()).isEqualTo("New Name");
                assertThat(response.getAvatarUrl()).isEqualTo("https://img.test/new.png");
        }

        @Test
        void should_enable_seller_role_for_current_user() {
                User existingUser = User.builder()
                                .id(11L)
                                .email("buyer@test.com")
                                .fullName("Buyer")
                                .roles("BUYER")
                                .password("encoded")
                                .build();

                when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(existingUser));
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                UserProfileResponse response = authService.enableSellerRole("buyer@test.com");

                assertThat(response.getRoles()).isEqualTo("BUYER,SELLER");
        }
}
