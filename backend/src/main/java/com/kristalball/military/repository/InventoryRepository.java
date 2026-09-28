package com.kristalball.military.repository;

import com.kristalball.military.entity.Asset;
import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByBaseAndAsset(Base base, Asset asset);

    @Query("""
            select i.asset.equipmentType.name as equipmentTypeName,
                   sum(i.totalQuantity) as totalQuantity,
                   sum(i.availableQuantity) as availableQuantity
            from Inventory i
            where (:baseId is null or i.base.id = :baseId)
              and (:equipmentTypeId is null or i.asset.equipmentType.id = :equipmentTypeId)
            group by i.asset.equipmentType.name
            """)
    List<DashboardInventoryTotal> findDashboardTotals(
            @Param("baseId") Long baseId,
            @Param("equipmentTypeId") Long equipmentTypeId);

    interface DashboardInventoryTotal {
        String getEquipmentTypeName();

        Long getTotalQuantity();

        Long getAvailableQuantity();
    }
}
