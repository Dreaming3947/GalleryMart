package com.gallerymart.backend.order.controller;

import com.gallerymart.backend.config.ApiResponse;
import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.order.dto.request.OrderCreateRequest;
import com.gallerymart.backend.order.dto.response.OrderResponse;
import com.gallerymart.backend.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        OrderResponse response = orderService.createOrder(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    @PatchMapping("/{id}/payment-sent")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<OrderResponse>> markPaymentSent(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        OrderResponse response = orderService.markPaymentSent(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as sent", response));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        OrderResponse response = orderService.confirmOrder(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Order confirmed", response));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        OrderResponse response = orderService.cancelOrder(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(currentUser)));
    }

    @GetMapping("/sales")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getSalesOrders(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getSalesOrders(currentUser)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id, currentUser)));
    }
}
