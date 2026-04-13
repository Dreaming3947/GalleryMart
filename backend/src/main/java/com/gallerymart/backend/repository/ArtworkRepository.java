package com.gallerymart.backend.repository;

import com.gallerymart.backend.entity.Artwork;
import com.gallerymart.backend.entity.enums.ArtworkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    List<Artwork> findBySellerId(Long sellerId);

    /**
     * Paginated search with optional filters. Only status IN (AVAILABLE, RESERVED) is returned.
     */
    @Query("""
            SELECT a FROM Artwork a
            WHERE a.status IN :statuses
            AND (:category IS NULL OR a.category = :category)
            AND (:minPrice IS NULL OR a.price >= :minPrice)
            AND (:maxPrice IS NULL OR a.price <= :maxPrice)
            AND (:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY a.createdAt DESC
            """)
    Page<Artwork> searchArtworks(
            @Param("statuses") List<ArtworkStatus> statuses,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
