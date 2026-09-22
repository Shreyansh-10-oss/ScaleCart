package com.scalecart.order.service;
import com.scalecart.inventory.entity.Inventory;
import com.scalecart.inventory.repository.InventoryRepository;
import com.scalecart.product.entity.Product;
import com.scalecart.product.repository.ProductRepository;
import com.scalecart.order.entity.OrderItem;
import com.scalecart.order.dto.OrderItemRequest;
import org.springframework.transaction.annotation.Transactional;
import com.scalecart.order.dto.OrderItemResponse;

import com.scalecart.order.dto.OrderRequest;
import com.scalecart.order.dto.OrderResponse;
import com.scalecart.order.entity.Order;
import com.scalecart.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import com.scalecart.exception.OrderNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.scalecart.order.dto.UpdateOrderRequest;
import java.math.BigDecimal;
import java.util.List;

import java.time.LocalDateTime;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public OrderService(OrderRepository orderRepository,ProductRepository productRepository,
                        InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .userId(request.getUserId())
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Product not found with id: "
                                            + itemRequest.getProductId()
                            )
                    );

            Inventory inventory = inventoryRepository
                    .findByProductIdForUpdate(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Inventory not found for product id: "
                                            + itemRequest.getProductId()
                            )
                    );

            int requestedQuantity = itemRequest.getQuantity();

            if (inventory.getQuantity() < requestedQuantity) {
                throw new RuntimeException("Insufficient stock");
            }

            inventory.setQuantity(
                    inventory.getQuantity() - requestedQuantity
            );

            inventoryRepository.save(inventory);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(requestedQuantity));

            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(requestedQuantity);
            orderItem.setPrice(product.getPrice());

            order.getItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);

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

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );

        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> {
                    OrderItemResponse itemResponse = new OrderItemResponse();

                    itemResponse.setProductId(item.getProduct().getId());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setPrice(item.getPrice());

                    return itemResponse;
                })
                .toList();

        response.setItems(itemResponses);

        return response;
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
