package com.shopsphere.repository;

import com.shopsphere.entity.Product;
import com.shopsphere.entity.ProductStatus;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where p.moderationStatus = :status or p.moderationStatus is null")
    List<Product> findVisibleWithStatus(@Param("status") ProductStatus status);
    List<Product> findBySeller_EmailIgnoreCase(String email);
    List<Product> findBySeller_EmailIgnoreCaseAndModerationStatus(String email, ProductStatus moderationStatus);
}
