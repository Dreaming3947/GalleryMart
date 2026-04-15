package com.gallerymart.backend.order.dto.response;

import com.gallerymart.backend.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private String note;
    private LocalDateTime paymentSentAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;

    private Long artworkId;
    private String artworkTitle;
    private String artworkImageUrl;
}
