package com.gallerymart.backend.entity;

import com.gallerymart.backend.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order entity representing a purchase transaction between buyer and seller.
 * Order status transitions: PENDING → CONFIRMED or CANCELLED.
 * A PENDING order expires after 48 hours (expiresAt field).
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_buyer_id", columnList = "buyer_id"),
        @Index(name = "idx_order_artwork_id", columnList = "artwork_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"buyer", "artwork"})
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who placed this order.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    /**
     * The artwork being purchased.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 1000)
    private String note;

    /**
     * Set when the buyer confirms they have sent payment via VietQR.
     * This does NOT change the order status — seller must still confirm.
     */
    @Column(name = "payment_sent_at")
    private LocalDateTime paymentSentAt;

    /**
     * Deadline for the order. PENDING orders past this time are auto-cancelled.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
