package com.gallerymart.backend.notification.service.impl;

import com.gallerymart.backend.entity.Notification;
import com.gallerymart.backend.entity.Order;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.entity.enums.OrderStatus;
import com.gallerymart.backend.notification.dto.response.NotificationResponse;
import com.gallerymart.backend.notification.service.NotificationService;
import com.gallerymart.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(User currentUser) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public int markAllRead(User currentUser) {
        return notificationRepository.markAllRead(currentUser.getId());
    }

    @Override
    @Transactional
    public void createOrderStatusNotification(Order order) {
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.CANCELLED) {
            return;
        }

        String title = order.getStatus() == OrderStatus.CONFIRMED
                ? "Order confirmed"
                : "Order cancelled";

        String message = order.getStatus() == OrderStatus.CONFIRMED
                ? "Your order #" + order.getId() + " for artwork '" + order.getArtwork().getTitle() + "' has been confirmed by the seller."
                : "Your order #" + order.getId() + " for artwork '" + order.getArtwork().getTitle() + "' has been cancelled by the seller.";

        Notification notification = Notification.builder()
                .user(order.getBuyer())
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
