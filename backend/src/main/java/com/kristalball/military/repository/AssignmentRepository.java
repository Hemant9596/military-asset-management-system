package com.kristalball.military.repository;

import com.kristalball.military.entity.Assignment;
import com.kristalball.military.entity.Base;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByBase(Base base, Pageable pageable);
}
