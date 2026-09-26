package com.kristalball.military.repository;

import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Asset;
import com.kristalball.military.entity.OpeningBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpeningBalanceRepository extends JpaRepository<OpeningBalance, Long> {
    boolean existsByBaseAndAsset(Base base, Asset asset);
}