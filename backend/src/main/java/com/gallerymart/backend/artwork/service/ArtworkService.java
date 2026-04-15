package com.gallerymart.backend.artwork.service;

import com.gallerymart.backend.artwork.dto.request.ArtworkUpsertRequest;
import com.gallerymart.backend.artwork.dto.response.ArtworkPageResponse;
import com.gallerymart.backend.artwork.dto.response.ArtworkResponse;
import com.gallerymart.backend.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface ArtworkService {

    ArtworkResponse createArtwork(ArtworkUpsertRequest request, User currentUser);

    ArtworkResponse updateArtwork(Long artworkId, ArtworkUpsertRequest request, User currentUser);

    void deleteArtwork(Long artworkId, User currentUser);

    ArtworkResponse getArtworkById(Long artworkId);

    ArtworkPageResponse searchArtworks(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword,
            int page,
            int size
    );

    List<ArtworkResponse> getMyArtworks(User currentUser);
}
