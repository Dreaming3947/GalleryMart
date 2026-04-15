package com.gallerymart.backend.order.scheduler;

import com.gallerymart.backend.entity.Artwork;
import com.gallerymart.backend.entity.Order;
import com.gallerymart.backend.entity.enums.ArtworkStatus;
import com.gallerymart.backend.entity.enums.OrderStatus;
import com.gallerymart.backend.notification.service.NotificationService;
import com.gallerymart.backend.repository.ArtworkRepository;
import com.gallerymart.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;
    private final ArtworkRepository artworkRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${order.expiration-check-interval-ms:60000}")
    @Transactional
    public void cancelExpiredPendingOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> expiredOrders = orderRepository.findByStatusAndExpiresAtBefore(OrderStatus.PENDING, now);

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.CANCELLED);

            Artwork artwork = order.getArtwork();
            if (artwork.getStatus() == ArtworkStatus.RESERVED) {
                artwork.setStatus(ArtworkStatus.AVAILABLE);
                artworkRepository.save(artwork);
            }

            Order savedOrder = orderRepository.save(order);
            notificationService.createOrderStatusNotification(savedOrder);
        }

        log.info("Auto-cancelled {} expired pending order(s)", expiredOrders.size());
    }
}
