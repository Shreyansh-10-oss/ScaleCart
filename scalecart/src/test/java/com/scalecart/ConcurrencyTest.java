package com.scalecart;

import com.scalecart.inventory.entity.Inventory;
import com.scalecart.inventory.repository.InventoryRepository;
import com.scalecart.order.dto.OrderItemRequest;
import com.scalecart.order.dto.OrderRequest;
import com.scalecart.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void testConcurrentPurchases() throws Exception {

        Long productId = 1L;

        // Reset inventory to 5 before the test
        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow();

        inventory.setQuantity(5);
        inventoryRepository.save(inventory);

        int numberOfRequests = 10;

        ExecutorService executorService =
                Executors.newFixedThreadPool(numberOfRequests);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch doneLatch =
                new CountDownLatch(numberOfRequests);

        List<Future<String>> results = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {

            Future<String> result = executorService.submit(() -> {

                try {

                    // Wait until all threads are ready
                    startLatch.await();

                    int maxRetries = 10;

                    for (int attempt = 1; attempt <= maxRetries; attempt++) {

                        try {

                            OrderItemRequest itemRequest =
                                    new OrderItemRequest();

                            itemRequest.setProductId(productId);
                            itemRequest.setQuantity(1);

                            OrderRequest orderRequest =
                                    new OrderRequest();

                            orderRequest.setUserId(1L);
                            orderRequest.setItems(
                                    List.of(itemRequest)
                            );

                            orderService.createOrder(orderRequest);

                            return "SUCCESS";

                        } catch (ObjectOptimisticLockingFailureException e) {

                            System.out.println(
                                    "Optimistic locking conflict. Retry: "
                                            + attempt
                            );

                            Thread.sleep(50);
                        }
                    }

                    return "FAILED: Maximum retries exceeded";

                } catch (Exception e) {

                    return "FAILED: "
                            + e.getClass().getSimpleName();

                } finally {

                    doneLatch.countDown();
                }
            });

            results.add(result);
        }

        // Start all threads
        startLatch.countDown();

        // Wait for all threads
        doneLatch.await();

        executorService.shutdown();

        int successCount = 0;
        int failureCount = 0;

        for (Future<String> result : results) {

            String response = result.get();

            System.out.println(response);

            if (response.equals("SUCCESS")) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        Inventory finalInventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow();

        System.out.println(
                "Successful purchases: " + successCount
        );

        System.out.println(
                "Failed purchases: " + failureCount
        );

        System.out.println(
                "Final inventory: "
                        + finalInventory.getQuantity()
        );

        System.out.println(
                "Final version: "
                        + finalInventory.getVersion()
        );

        // Inventory must never become negative
        assertTrue(
                finalInventory.getQuantity() >= 0
        );

        // 5 items were available
        // Therefore at most 5 purchases can succeed
        assertTrue(successCount <= 5);

        // If all available stock was successfully sold,
        // inventory should be zero.
        assertEquals(0, finalInventory.getQuantity());
    }
}