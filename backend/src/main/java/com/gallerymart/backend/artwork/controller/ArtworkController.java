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

import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/artworks")
@RequiredArgsConstructor
@Validated
public class ArtworkController {

    private final ArtworkService artworkService;

    // ... (searchArtworks and getArtwork methods remain same)

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ArtworkResponse>> createArtwork(
            @Valid @RequestPart("artwork") ArtworkUpsertRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User currentUser
    ) {
        // Luu y: Trong thuc te, artworkService.createArtwork se can xu ly MultipartFile image
        ArtworkResponse response = artworkService.createArtwork(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Artwork created successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ArtworkResponse>> updateArtwork(
            @PathVariable Long id,
            @Valid @RequestPart("artwork") ArtworkUpsertRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
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
