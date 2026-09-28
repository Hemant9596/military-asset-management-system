package com.kristalball.military.repository;

import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Page<Purchase> findByBase(Base base, Pageable pageable);

    @Query("""
            select p.purchaseDate as eventDate, sum(p.quantity) as quantity
            from Purchase p
            where (:baseId is null or p.base.id = :baseId)
              and (:equipmentTypeId is null or p.asset.equipmentType.id = :equipmentTypeId)
              and (:fromDate is null or p.purchaseDate >= :fromDate)
            group by p.purchaseDate
            """)
    List<DashboardDailyTotal> findDashboardDailyTotals(
            @Param("baseId") Long baseId,
            @Param("equipmentTypeId") Long equipmentTypeId,
            @Param("fromDate") LocalDate fromDate);

    interface DashboardDailyTotal {
        LocalDate getEventDate();

        Long getQuantity();
    }
}
