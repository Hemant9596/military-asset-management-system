package com.kristalball.military.repository;

import com.kristalball.military.entity.Asset;
import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByBaseAndAsset(Base base, Asset asset);
}
