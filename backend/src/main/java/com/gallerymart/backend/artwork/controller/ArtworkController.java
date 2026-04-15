package com.gallerymart.backend.artwork.controller;

import com.gallerymart.backend.artwork.dto.request.ArtworkUpsertRequest;
import com.gallerymart.backend.artwork.dto.response.ArtworkPageResponse;
import com.gallerymart.backend.artwork.dto.response.ArtworkResponse;
import com.gallerymart.backend.artwork.service.ArtworkService;
import com.gallerymart.backend.config.ApiResponse;
import com.gallerymart.backend.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/artworks")
@RequiredArgsConstructor
@Validated
public class ArtworkController {

    private final ArtworkService artworkService;

    @GetMapping
    public ResponseEntity<ApiResponse<ArtworkPageResponse>> searchArtworks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be >= 0") int page,
            @RequestParam(defaultValue = "12") @Min(value = 1, message = "Size must be >= 1") @Max(value = 100, message = "Size must be <= 100") int size
    ) {
        ArtworkPageResponse response = artworkService.searchArtworks(category, minPrice, maxPrice, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtworkResponse>> getArtworkById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(artworkService.getArtworkById(id)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<ArtworkResponse>>> getMyArtworks(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(artworkService.getMyArtworks(currentUser)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ArtworkResponse>> createArtwork(
            @Valid @RequestBody ArtworkUpsertRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ArtworkResponse response = artworkService.createArtwork(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Artwork created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ArtworkResponse>> updateArtwork(
            @PathVariable Long id,
            @Valid @RequestBody ArtworkUpsertRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ArtworkResponse response = artworkService.updateArtwork(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Artwork updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteArtwork(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        artworkService.deleteArtwork(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Artwork deleted successfully", null));
    }
}
