package com.gallerymart.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * In-app notification sent to users when order status changes.
 * Notifications are created by the system — buyers receive them when sellers
 * confirm or cancel their orders.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_user_read", columnList = "user_id, is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The recipient of this notification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, name = "is_read")
    @Builder.Default
    private boolean isRead = false;
}
