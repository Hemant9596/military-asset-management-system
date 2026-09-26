package com.kristalball.military.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_base_id", nullable = false)
    private Base sourceBase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_base_id", nullable = false)
    private Base destinationBase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Column(nullable = false)
    private LocalDate transferDate;

    @Column
    private String referenceNumber;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Transfer() {
    }

    public Transfer(Long id, Base sourceBase, Base destinationBase, Asset asset, User createdBy, Integer quantity,
            LocalDate transferDate, String referenceNumber, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.sourceBase = sourceBase;
        this.destinationBase = destinationBase;
        this.asset = asset;
        this.createdBy = createdBy;
        this.quantity = quantity;
        this.transferDate = transferDate;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Base getSourceBase() {
        return sourceBase;
    }

    public void setSourceBase(Base sourceBase) {
        this.sourceBase = sourceBase;
    }

    public Base getDestinationBase() {
        return destinationBase;
    }

    public void setDestinationBase(Base destinationBase) {
        this.destinationBase = destinationBase;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDate transferDate) {
        this.transferDate = transferDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Base sourceBase;
        private Base destinationBase;
        private Asset asset;
        private User createdBy;
        private Integer quantity;
        private LocalDate transferDate;
        private String referenceNumber;
        private String notes;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder sourceBase(Base sourceBase) {
            this.sourceBase = sourceBase;
            return this;
        }

        public Builder destinationBase(Base destinationBase) {
            this.destinationBase = destinationBase;
            return this;
        }

        public Builder asset(Asset asset) {
            this.asset = asset;
            return this;
        }

        public Builder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder transferDate(LocalDate transferDate) {
            this.transferDate = transferDate;
            return this;
        }

        public Builder referenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Transfer build() {
            return new Transfer(id, sourceBase, destinationBase, asset, createdBy, quantity, transferDate,
                    referenceNumber, notes, createdAt);
        }
    }
}
