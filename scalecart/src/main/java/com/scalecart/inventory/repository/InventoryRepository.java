package com.scalecart.inventory.repository;
import com.scalecart.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long>{
    Optional<Inventory> findByProductId(Long productId);
}
