package com.shopsphere.dto;

import java.math.BigDecimal;
import com.shopsphere.entity.ProductStatus;

public class ProductResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private Long sellerId;
    private String sellerName;
    private ProductStatus moderationStatus;

    public ProductResponse() {
    }

    public ProductResponse(
            Long id,
            String name,
            BigDecimal price,
            String description,
            Integer stock,
            Long categoryId,
            String categoryName
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public ProductResponse(Long id, String name, BigDecimal price, String description, Integer stock,
            Long categoryId, String categoryName, String imageUrl, Long sellerId, String sellerName,
            ProductStatus moderationStatus) {
        this(id, name, price, description, stock, categoryId, categoryName);
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.moderationStatus = moderationStatus;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public Integer getStock() {
        return stock;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImageUrl() { return imageUrl; }
    public Long getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public ProductStatus getModerationStatus() { return moderationStatus; }
}
