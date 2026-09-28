package com.kristalball.military.repository;

import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Asset;
import com.kristalball.military.entity.OpeningBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OpeningBalanceRepository extends JpaRepository<OpeningBalance, Long> {
    boolean existsByBaseAndAsset(Base base, Asset asset);

    @Query("""
            select b.effectiveDate as eventDate, sum(b.quantity) as quantity
            from OpeningBalance b
            where (:baseId is null or b.base.id = :baseId)
              and (:equipmentTypeId is null or b.asset.equipmentType.id = :equipmentTypeId)
              and (:fromDate is null or b.effectiveDate >= :fromDate)
            group by b.effectiveDate
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