package com.shopsphere.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name must not be blank")
    private String name;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    private BigDecimal price;

    private String description;

    @Column(length = 2048)
    private String imageUrl;

    @NotNull(message = "Product stock is required")
    @PositiveOrZero(message = "Product stock must not be negative")
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private AccountUser seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", length = 24)
    private ProductStatus moderationStatus = ProductStatus.ACTIVE;

    protected Product() {
        
    }

    public Product(
            String name,
            BigDecimal price,
            String description,
            Integer stock
    ) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public AccountUser getSeller() { return seller; }

    public void setSeller(AccountUser seller) { this.seller = seller; }

    public ProductStatus getModerationStatus() {
        // Existing catalog rows predate moderation and remain visible after schema update.
        return moderationStatus == null ? ProductStatus.ACTIVE : moderationStatus;
    }

    public void setModerationStatus(ProductStatus moderationStatus) { this.moderationStatus = moderationStatus; }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
