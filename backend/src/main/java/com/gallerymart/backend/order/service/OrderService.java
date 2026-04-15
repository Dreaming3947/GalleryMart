package com.gallerymart.backend.order.service;

import com.gallerymart.backend.entity.User;
import com.gallerymart.backend.order.dto.request.OrderCreateRequest;
import com.gallerymart.backend.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request, User currentUser);

    OrderResponse markPaymentSent(Long orderId, User currentUser);

    OrderResponse confirmOrder(Long orderId, User currentUser);

    OrderResponse cancelOrder(Long orderId, User currentUser);

    List<OrderResponse> getMyOrders(User currentUser);

    List<OrderResponse> getSalesOrders(User currentUser);

    OrderResponse getOrderById(Long orderId, User currentUser);
}
