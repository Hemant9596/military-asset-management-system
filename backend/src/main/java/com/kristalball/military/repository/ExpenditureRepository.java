package com.kristalball.military.repository;

import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Expenditure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {
    Page<Expenditure> findByBase(Base base, Pageable pageable);

    @Query("""
            select e.expenditureDate as eventDate, sum(e.quantity) as quantity
            from Expenditure e
            where (:baseId is null or e.base.id = :baseId)
              and (:equipmentTypeId is null or e.asset.equipmentType.id = :equipmentTypeId)
              and (:fromDate is null or e.expenditureDate >= :fromDate)
            group by e.expenditureDate
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
