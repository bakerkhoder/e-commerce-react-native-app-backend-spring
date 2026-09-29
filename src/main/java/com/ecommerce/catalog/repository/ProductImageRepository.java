package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}