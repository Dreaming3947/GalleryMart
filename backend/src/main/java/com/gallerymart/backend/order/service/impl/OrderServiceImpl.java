package com.gallerymart.backend.order.service.impl;

import com.gallerymart.backend.entity.Artwork;
import com.gallerymart.backend.entity.Order;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.entity.enums.ArtworkStatus;
import com.gallerymart.backend.entity.enums.OrderStatus;
import com.gallerymart.backend.exception.ForbiddenException;
import com.gallerymart.backend.exception.InvalidInputException;
import com.gallerymart.backend.exception.ResourceNotFoundException;
import com.gallerymart.backend.order.dto.request.OrderCreateRequest;
import com.gallerymart.backend.order.dto.response.OrderResponse;
import com.gallerymart.backend.order.service.OrderService;
import com.gallerymart.backend.repository.ArtworkRepository;
import com.gallerymart.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int ORDER_EXPIRE_HOURS = 48;

    private final OrderRepository orderRepository;
    private final ArtworkRepository artworkRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, User currentUser) {
        requireBuyerRole(currentUser);

        Artwork artwork = artworkRepository.findById(request.getArtworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found with id: " + request.getArtworkId()));

        if (artwork.getSeller().getId().equals(currentUser.getId())) {
            throw new InvalidInputException("You cannot buy your own artwork");
        }

        if (artwork.getStatus() == ArtworkStatus.SOLD) {
            throw new InvalidInputException("Artwork is already sold");
        }

        if (artwork.getStatus() != ArtworkStatus.AVAILABLE) {
            throw new InvalidInputException("Artwork is not available for ordering");
        }

        if (orderRepository.existsByArtworkIdAndStatus(artwork.getId(), OrderStatus.PENDING)) {
            throw new InvalidInputException("This artwork already has a pending order");
        }

        Order order = Order.builder()
                .buyer(currentUser)
                .artwork(artwork)
                .status(OrderStatus.PENDING)
                .totalPrice(artwork.getPrice())
                .note(trimToNull(request.getNote()))
                .expiresAt(LocalDateTime.now().plusHours(ORDER_EXPIRE_HOURS))
                .build();

        artwork.setStatus(ArtworkStatus.RESERVED);
        artworkRepository.save(artwork);

        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse markPaymentSent(Long orderId, User currentUser) {
        requireBuyerRole(currentUser);
        Order order = loadOrder(orderId);
        verifyBuyer(order, currentUser);
        ensurePending(order);

        order.setPaymentSentAt(LocalDateTime.now());
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse confirmOrder(Long orderId, User currentUser) {
        requireSellerRole(currentUser);
        Order order = loadOrder(orderId);
        verifySeller(order, currentUser);
        ensurePending(order);

        order.setStatus(OrderStatus.CONFIRMED);
        Artwork artwork = order.getArtwork();
        artwork.setStatus(ArtworkStatus.SOLD);

        artworkRepository.save(artwork);
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, User currentUser) {
        requireSellerRole(currentUser);
        Order order = loadOrder(orderId);
        verifySeller(order, currentUser);
        ensurePending(order);

        order.setStatus(OrderStatus.CANCELLED);
        Artwork artwork = order.getArtwork();
        artwork.setStatus(ArtworkStatus.AVAILABLE);

        artworkRepository.save(artwork);
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(User currentUser) {
        requireBuyerRole(currentUser);
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getSalesOrders(User currentUser) {
        requireSellerRole(currentUser);
        return orderRepository.findByArtworkSellerIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, User currentUser) {
        Order order = loadOrder(orderId);
        boolean isBuyer = order.getBuyer().getId().equals(currentUser.getId());
        boolean isSeller = order.getArtwork().getSeller().getId().equals(currentUser.getId());

        if (!isBuyer && !isSeller) {
            throw new ForbiddenException("You are not allowed to view this order");
        }

        return mapToResponse(order);
    }

    private Order loadOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private void ensurePending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidInputException("Only PENDING orders can perform this action");
        }
    }

    private void verifyBuyer(Order order, User currentUser) {
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not the buyer of this order");
        }
    }

    private void verifySeller(Order order, User currentUser) {
        if (!order.getArtwork().getSeller().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not the seller of this order");
        }
    }

    private void requireBuyerRole(User currentUser) {
        if (!currentUser.hasRole("BUYER")) {
            throw new ForbiddenException("Only buyers can perform this action");
        }
    }

    private void requireSellerRole(User currentUser) {
        if (!currentUser.hasRole("SELLER")) {
            throw new ForbiddenException("Only sellers can perform this action");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .note(order.getNote())
                .paymentSentAt(order.getPaymentSentAt())
                .expiresAt(order.getExpiresAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .buyerId(order.getBuyer().getId())
                .buyerName(order.getBuyer().getFullName())
                .sellerId(order.getArtwork().getSeller().getId())
                .sellerName(order.getArtwork().getSeller().getFullName())
                .artworkId(order.getArtwork().getId())
                .artworkTitle(order.getArtwork().getTitle())
                .artworkImageUrl(order.getArtwork().getImageUrl())
                .build();
    }
}
