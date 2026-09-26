package com.kristalball.military.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opening_balances", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "base_id", "asset_id" })
})
public class OpeningBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OpeningBalance() {
    }

    public OpeningBalance(Base base, Asset asset, User createdBy, Integer quantity, LocalDate effectiveDate) {
        this.base = base;
        this.asset = asset;
        this.createdBy = createdBy;
        this.quantity = quantity;
        this.effectiveDate = effectiveDate;
    }

    public Long getId() {
        return id;
    }

    public Base getBase() {
        return base;
    }

    public Asset getAsset() {
        return asset;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}