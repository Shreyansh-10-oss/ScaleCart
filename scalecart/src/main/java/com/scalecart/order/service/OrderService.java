package com.scalecart.order.service;

import com.scalecart.order.dto.OrderRequest;
import com.scalecart.order.dto.OrderResponse;
import com.scalecart.order.entity.Order;
import com.scalecart.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import com.scalecart.exception.OrderNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.scalecart.order.dto.UpdateOrderRequest;

import java.time.LocalDateTime;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse createOrder(OrderRequest request) {

        Order order = Order.builder()
                .userId(request.getUserId())
                .totalAmount(request.getTotalAmount())
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {

        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(this::mapToResponse);
    }

    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order with id " + id + " not found"
                        )
                );

        order.setTotalAmount(request.getTotalAmount());

        Order updatedOrder = orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }


    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + id + " not found"));

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order with id " + id + " not found"
                        )
                );

        orderRepository.delete(order);
    }
}
