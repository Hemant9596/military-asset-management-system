package com.kristalball.military.repository;

import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Page<Transfer> findBySourceBaseOrDestinationBase(Base sourceBase, Base destinationBase, Pageable pageable);

    @Query("""
            select t.transferDate as eventDate,
                   t.sourceBase.id as sourceBaseId,
                   t.sourceBase.name as sourceBaseName,
                   t.destinationBase.id as destinationBaseId,
                   t.destinationBase.name as destinationBaseName,
                   sum(t.quantity) as quantity
            from Transfer t
            where (:baseId is null or t.sourceBase.id = :baseId or t.destinationBase.id = :baseId)
              and (:equipmentTypeId is null or t.asset.equipmentType.id = :equipmentTypeId)
              and (:fromDate is null or t.transferDate >= :fromDate)
            group by t.transferDate, t.sourceBase.id, t.sourceBase.name,
                     t.destinationBase.id, t.destinationBase.name
            """)
    List<DashboardTransferTotal> findDashboardDailyTotals(
            @Param("baseId") Long baseId,
            @Param("equipmentTypeId") Long equipmentTypeId,
            @Param("fromDate") LocalDate fromDate);

    interface DashboardTransferTotal {
        LocalDate getEventDate();

        Long getSourceBaseId();

        String getSourceBaseName();

        Long getDestinationBaseId();

        String getDestinationBaseName();

        Long getQuantity();
    }
}
