package com.gallerymart.backend.notification.service;

import com.gallerymart.backend.entity.Order;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.notification.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications(User currentUser);

    int markAllRead(User currentUser);

    void createOrderStatusNotification(Order order);
}
