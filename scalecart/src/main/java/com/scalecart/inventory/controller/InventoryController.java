package com.scalecart.inventory.controller;

import com.scalecart.inventory.dto.InventoryRequest;
import com.scalecart.inventory.dto.InventoryResponse;
import com.scalecart.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(
            @Valid @RequestBody InventoryRequest request) {

        return inventoryService.createInventory(request);
    }

    @GetMapping("/{productId}")
    public InventoryResponse getInventory(
            @PathVariable Long productId) {

        return inventoryService.getInventoryByProductId(productId);
    }
    @PutMapping("/{productId}/add")
    public InventoryResponse addStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return inventoryService.addStock(productId, quantity);
    }
    @PutMapping("/{productId}/remove")
    public InventoryResponse removeStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return inventoryService.removeStock(productId, quantity);
    }
}
