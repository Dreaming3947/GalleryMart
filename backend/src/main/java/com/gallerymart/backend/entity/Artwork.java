package com.gallerymart.backend.entity;

import com.gallerymart.backend.entity.enums.ArtworkStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Artwork entity representing a painting or art piece listed for sale.
 * An artwork transitions through AVAILABLE → RESERVED → SOLD.
 */
@Entity
@Table(name = "artworks", indexes = {
        @Index(name = "idx_artwork_seller_id", columnList = "seller_id"),
        @Index(name = "idx_artwork_status", columnList = "status"),
        @Index(name = "idx_artwork_category", columnList = "category"),
        @Index(name = "idx_artwork_seller_status", columnList = "seller_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"seller"})
public class Artwork extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The seller who listed this artwork.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ArtworkStatus status = ArtworkStatus.AVAILABLE;

    @Column(length = 100)
    private String category;
}
