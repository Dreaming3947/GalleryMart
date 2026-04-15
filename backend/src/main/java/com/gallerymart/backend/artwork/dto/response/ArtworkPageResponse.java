package com.gallerymart.backend.artwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtworkPageResponse {
    private List<ArtworkResponse> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
}
