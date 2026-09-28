package com.kristalball.military.repository;

import com.kristalball.military.entity.Assignment;
import com.kristalball.military.entity.Base;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByBase(Base base, Pageable pageable);

    @Query("""
            select sum(a.quantity)
            from Assignment a
            where (:baseId is null or a.base.id = :baseId)
                and (:equipmentTypeId is null or a.asset.equipmentType.id = :equipmentTypeId)
                and (:fromDate is null or a.assignmentDate >= :fromDate)
                and (:toDate is null or a.assignmentDate <= :toDate)
            """)
    Long sumDashboardQuantities(
            @Param("baseId") Long baseId,
            @Param("equipmentTypeId") Long equipmentTypeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
