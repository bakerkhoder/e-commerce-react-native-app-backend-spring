// ProductRepository.java
package com.ecommerce.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.catalog.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}