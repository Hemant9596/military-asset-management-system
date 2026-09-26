package com.kristalball.military.repository;

import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Page<Transfer> findBySourceBaseOrDestinationBase(Base sourceBase, Base destinationBase, Pageable pageable);
}
