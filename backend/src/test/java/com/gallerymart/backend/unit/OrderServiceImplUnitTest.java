package com.gallerymart.backend.unit;

import com.gallerymart.backend.entity.Artwork;
import com.gallerymart.backend.entity.Order;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.entity.enums.ArtworkStatus;
import com.gallerymart.backend.entity.enums.OrderStatus;
import com.gallerymart.backend.exception.ForbiddenException;
import com.gallerymart.backend.exception.InvalidInputException;
import com.gallerymart.backend.notification.service.NotificationService;
import com.gallerymart.backend.order.dto.request.OrderCreateRequest;
import com.gallerymart.backend.order.dto.response.OrderResponse;
import com.gallerymart.backend.order.service.impl.OrderServiceImpl;
import com.gallerymart.backend.repository.ArtworkRepository;
import com.gallerymart.backend.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ArtworkRepository artworkRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void should_throw_invalid_input_when_buyer_orders_own_artwork() {
        User buyer = User.builder().id(1L).roles("BUYER").build();
        Artwork artwork = Artwork.builder()
                .id(5L)
                .seller(buyer)
                .status(ArtworkStatus.AVAILABLE)
                .price(new BigDecimal("50.00"))
                .build();

        when(artworkRepository.findById(5L)).thenReturn(Optional.of(artwork));

        OrderCreateRequest request = OrderCreateRequest.builder().artworkId(5L).build();

        assertThatThrownBy(() -> orderService.createOrder(request, buyer))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("cannot buy your own artwork");

        verifyNoInteractions(notificationService);
    }

    @Test
    void should_confirm_order_set_artwork_sold_and_create_notification() {
        User seller = User.builder().id(2L).roles("SELLER").fullName("Seller").build();
        User buyer = User.builder().id(3L).roles("BUYER").fullName("Buyer").build();

        Artwork artwork = Artwork.builder()
                .id(11L)
                .seller(seller)
                .title("Test Art")
                .imageUrl("img")
                .status(ArtworkStatus.RESERVED)
                .price(new BigDecimal("100.00"))
                .build();

        Order order = Order.builder()
                .id(22L)
                .buyer(buyer)
                .artwork(artwork)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("100.00"))
                .build();

        when(orderRepository.findById(22L)).thenReturn(Optional.of(order));
        when(artworkRepository.save(any(Artwork.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.confirmOrder(22L, seller);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getArtwork().getStatus()).isEqualTo(ArtworkStatus.SOLD);
        verify(notificationService).createOrderStatusNotification(order);
    }

    @Test
    void should_throw_forbidden_when_non_buyer_marks_payment_sent() {
        User actualBuyer = User.builder().id(7L).roles("BUYER").build();
        User anotherBuyer = User.builder().id(8L).roles("BUYER").build();
        User seller = User.builder().id(9L).roles("SELLER").build();

        Artwork artwork = Artwork.builder()
                .id(33L)
                .seller(seller)
                .status(ArtworkStatus.RESERVED)
                .price(new BigDecimal("90.00"))
                .build();

        Order order = Order.builder()
                .id(44L)
                .buyer(actualBuyer)
                .artwork(artwork)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("90.00"))
                .build();

        when(orderRepository.findById(44L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markPaymentSent(44L, anotherBuyer))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not the buyer");
    }
}
