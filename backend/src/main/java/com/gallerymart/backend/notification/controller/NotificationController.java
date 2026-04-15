package com.gallerymart.backend.notification.controller;

import com.gallerymart.backend.config.ApiResponse;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.notification.dto.response.NotificationResponse;
import com.gallerymart.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(currentUser)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead(
            @AuthenticationPrincipal User currentUser
    ) {
        int updatedCount = notificationService.markAllRead(currentUser);
        return ResponseEntity.ok(
                ApiResponse.success("All notifications marked as read", Map.of("updatedCount", updatedCount))
        );
    }
}
