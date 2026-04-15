package com.gallerymart.backend.artwork.dto.response;

import com.gallerymart.backend.entity.enums.ArtworkStatus;
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
public class ArtworkResponse {
    private Long id;
    private Long sellerId;
    private String sellerName;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private ArtworkStatus status;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
