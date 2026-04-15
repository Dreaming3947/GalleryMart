package com.gallerymart.backend.artwork.service.impl;

import com.gallerymart.backend.artwork.dto.request.ArtworkUpsertRequest;
import com.gallerymart.backend.artwork.dto.response.ArtworkPageResponse;
import com.gallerymart.backend.artwork.dto.response.ArtworkResponse;
import com.gallerymart.backend.artwork.service.ArtworkService;
import com.gallerymart.backend.entity.Artwork;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.entity.enums.ArtworkStatus;
import com.gallerymart.backend.exception.ForbiddenException;
import com.gallerymart.backend.exception.InvalidInputException;
import com.gallerymart.backend.exception.ResourceNotFoundException;
import com.gallerymart.backend.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtworkServiceImpl implements ArtworkService {

    private final ArtworkRepository artworkRepository;

    @Override
    @Transactional
    public ArtworkResponse createArtwork(ArtworkUpsertRequest request, User currentUser) {
        requireSellerRole(currentUser);

        Artwork artwork = Artwork.builder()
                .seller(currentUser)
                .title(request.getTitle().trim())
                .description(trimToNull(request.getDescription()))
                .price(request.getPrice())
                .imageUrl(trimToNull(request.getImageUrl()))
                .category(trimToNull(request.getCategory()))
                .status(ArtworkStatus.AVAILABLE)
                .build();

        return mapToResponse(artworkRepository.save(artwork));
    }

    @Override
    @Transactional
    public ArtworkResponse updateArtwork(Long artworkId, ArtworkUpsertRequest request, User currentUser) {
        requireSellerRole(currentUser);
        Artwork artwork = loadArtwork(artworkId);
        verifyOwner(artwork, currentUser);

        artwork.setTitle(request.getTitle().trim());
        artwork.setDescription(trimToNull(request.getDescription()));
        artwork.setPrice(request.getPrice());
        artwork.setImageUrl(trimToNull(request.getImageUrl()));
        artwork.setCategory(trimToNull(request.getCategory()));

        return mapToResponse(artworkRepository.save(artwork));
    }

    @Override
    @Transactional
    public void deleteArtwork(Long artworkId, User currentUser) {
        requireSellerRole(currentUser);
        Artwork artwork = loadArtwork(artworkId);
        verifyOwner(artwork, currentUser);

        if (artwork.getStatus() != ArtworkStatus.AVAILABLE) {
            throw new InvalidInputException("Only AVAILABLE artworks can be deleted");
        }

        artworkRepository.delete(artwork);
    }

    @Override
    @Transactional(readOnly = true)
    public ArtworkResponse getArtworkById(Long artworkId) {
        return mapToResponse(loadArtwork(artworkId));
    }

    @Override
    @Transactional(readOnly = true)
    public ArtworkPageResponse searchArtworks(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword,
            int page,
            int size
    ) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidInputException("minPrice cannot be greater than maxPrice");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Artwork> artworkPage = artworkRepository.searchArtworks(
                List.of(ArtworkStatus.AVAILABLE, ArtworkStatus.RESERVED),
                trimToNull(category),
                minPrice,
                maxPrice,
                trimToNull(keyword),
                pageable
        );

        List<ArtworkResponse> items = artworkPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return ArtworkPageResponse.builder()
                .items(items)
                .page(artworkPage.getNumber())
                .size(artworkPage.getSize())
                .totalItems(artworkPage.getTotalElements())
                .totalPages(artworkPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtworkResponse> getMyArtworks(User currentUser) {
        requireSellerRole(currentUser);
        return artworkRepository.findBySellerIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Artwork loadArtwork(Long artworkId) {
        return artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found with id: " + artworkId));
    }

    private void requireSellerRole(User currentUser) {
        if (!currentUser.hasRole("SELLER")) {
            throw new ForbiddenException("Only sellers can manage artworks");
        }
    }

    private void verifyOwner(Artwork artwork, User currentUser) {
        if (!artwork.getSeller().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not own this artwork");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ArtworkResponse mapToResponse(Artwork artwork) {
        return ArtworkResponse.builder()
                .id(artwork.getId())
                .sellerId(artwork.getSeller().getId())
                .sellerName(artwork.getSeller().getFullName())
                .title(artwork.getTitle())
                .description(artwork.getDescription())
                .price(artwork.getPrice())
                .imageUrl(artwork.getImageUrl())
                .status(artwork.getStatus())
                .category(artwork.getCategory())
                .createdAt(artwork.getCreatedAt())
                .updatedAt(artwork.getUpdatedAt())
                .build();
    }
}
