package com.gallerymart.backend.unit;

import com.gallerymart.backend.artwork.dto.request.ArtworkUpsertRequest;
import com.gallerymart.backend.artwork.service.impl.ArtworkServiceImpl;
import com.gallerymart.backend.entity.Artwork;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.entity.enums.ArtworkStatus;
import com.gallerymart.backend.exception.ForbiddenException;
import com.gallerymart.backend.exception.InvalidInputException;
import com.gallerymart.backend.repository.ArtworkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtworkServiceImplUnitTest {

    @Mock
    private ArtworkRepository artworkRepository;

    @InjectMocks
    private ArtworkServiceImpl artworkService;

    @Test
    void should_throw_forbidden_when_non_seller_creates_artwork() {
        User buyer = User.builder().id(1L).roles("BUYER").build();
        ArtworkUpsertRequest request = ArtworkUpsertRequest.builder()
                .title("Test")
                .price(new BigDecimal("10.00"))
                .build();

        assertThatThrownBy(() -> artworkService.createArtwork(request, buyer))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only sellers");

        verifyNoInteractions(artworkRepository);
    }

    @Test
    void should_throw_invalid_input_when_search_price_range_is_invalid() {
        assertThatThrownBy(() -> artworkService.searchArtworks(
                null,
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                null,
                0,
                10
        )).isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("minPrice cannot be greater than maxPrice");
    }

    @Test
    void should_delete_artwork_when_owner_is_seller_and_status_available() {
        User seller = User.builder().id(9L).roles("SELLER").build();
        Artwork artwork = Artwork.builder()
                .id(100L)
                .seller(seller)
                .status(ArtworkStatus.AVAILABLE)
                .title("A")
                .price(new BigDecimal("20.00"))
                .build();

        when(artworkRepository.findById(100L)).thenReturn(Optional.of(artwork));

        artworkService.deleteArtwork(100L, seller);

        verify(artworkRepository).delete(artwork);
    }
}
