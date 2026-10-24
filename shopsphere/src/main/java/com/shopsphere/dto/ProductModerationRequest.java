package com.shopsphere.dto;

import com.shopsphere.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

public class ProductModerationRequest {
    @NotNull
    private ProductStatus status;

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
}
