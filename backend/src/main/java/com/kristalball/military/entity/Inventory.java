package com.kristalball.military.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "base_id", "asset_id" })
})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private Integer totalQuantity = 0;

    @Column(nullable = false)
    private Integer availableQuantity = 0;

    @Column(nullable = false)
    private Integer assignedQuantity = 0;

    @Column(nullable = false)
    private Integer expendedQuantity = 0;

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Inventory() {
    }

    public Inventory(Long id, Base base, Asset asset, Integer totalQuantity, Integer availableQuantity,
            Integer assignedQuantity, Integer expendedQuantity, LocalDateTime lastUpdated) {
        this.id = id;
        this.base = base;
        this.asset = asset;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.assignedQuantity = assignedQuantity;
        this.expendedQuantity = expendedQuantity;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getAssignedQuantity() {
        return assignedQuantity;
    }

    public void setAssignedQuantity(Integer assignedQuantity) {
        this.assignedQuantity = assignedQuantity;
    }

    public Integer getExpendedQuantity() {
        return expendedQuantity;
    }

    public void setExpendedQuantity(Integer expendedQuantity) {
        this.expendedQuantity = expendedQuantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Base base;
        private Asset asset;
        private Integer totalQuantity = 0;
        private Integer availableQuantity = 0;
        private Integer assignedQuantity = 0;
        private Integer expendedQuantity = 0;
        private LocalDateTime lastUpdated = LocalDateTime.now();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder base(Base base) {
            this.base = base;
            return this;
        }

        public Builder asset(Asset asset) {
            this.asset = asset;
            return this;
        }

        public Builder totalQuantity(Integer totalQuantity) {
            this.totalQuantity = totalQuantity;
            return this;
        }

        public Builder availableQuantity(Integer availableQuantity) {
            this.availableQuantity = availableQuantity;
            return this;
        }

        public Builder assignedQuantity(Integer assignedQuantity) {
            this.assignedQuantity = assignedQuantity;
            return this;
        }

        public Builder expendedQuantity(Integer expendedQuantity) {
            this.expendedQuantity = expendedQuantity;
            return this;
        }

        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Inventory build() {
            return new Inventory(id, base, asset, totalQuantity, availableQuantity, assignedQuantity, expendedQuantity,
                    lastUpdated);
        }
    }
}
