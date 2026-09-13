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
}
