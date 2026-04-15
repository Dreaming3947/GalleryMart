package com.gallerymart.backend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "Artwork id is required")
    private Long artworkId;

    @Size(max = 1000, message = "Note cannot exceed 1000 characters")
    private String note;
}
