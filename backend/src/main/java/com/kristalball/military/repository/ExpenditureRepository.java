package com.kristalball.military.repository;

import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Expenditure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {
    Page<Expenditure> findByBase(Base base, Pageable pageable);
}
