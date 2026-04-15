package com.gallerymart.backend.repository;

import com.gallerymart.backend.entity.Order;
import com.gallerymart.backend.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    /**
     * Returns all orders where the artwork belongs to the specified seller.
     */
    @Query("""
            SELECT o FROM Order o
            JOIN o.artwork a
            WHERE a.seller.id = :sellerId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findByArtworkSellerIdOrderByCreatedAtDesc(@Param("sellerId") Long sellerId);

    /**
     * Used by the scheduled job to find expired pending orders.
     */
    List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, LocalDateTime now);

    boolean existsByArtworkIdAndStatus(Long artworkId, OrderStatus status);
}
