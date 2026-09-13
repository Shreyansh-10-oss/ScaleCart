package com.scalecart.inventory.service;
import com.scalecart.inventory.dto.InventoryRequest;
import com.scalecart.inventory.dto.InventoryResponse;
import com.scalecart.inventory.entity.Inventory;
import com.scalecart.inventory.repository.InventoryRepository;
import com.scalecart.product.entity.Product;
import com.scalecart.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository) {

        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    public InventoryResponse createInventory(InventoryRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with id: "
                                        + request.getProductId()
                        ));

        if (inventoryRepository
                .findByProductId(request.getProductId())
                .isPresent()) {

            throw new RuntimeException(
                    "Inventory already exists for product id: "
                            + request.getProductId()
            );
        }

        Inventory inventory = new Inventory();

        inventory.setProduct(product);
        inventory.setQuantity(request.getQuantity());

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        return mapToResponse(savedInventory);
    }

    public InventoryResponse getInventoryByProductId(Long productId) {

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inventory not found for product id: "
                                        + productId
                        ));

        return mapToResponse(inventory);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getQuantity()
        );
    }

    public InventoryResponse addStock(Long productId, Integer quantity) {

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inventory not found for product id: " + productId
                        ));

        inventory.setQuantity(inventory.getQuantity() + quantity);

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return mapToResponse(updatedInventory);
    }
    public InventoryResponse removeStock(Long productId, Integer quantity) {

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inventory not found for product id: " + productId
                        ));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return mapToResponse(updatedInventory);
    }
}
