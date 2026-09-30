package com.ecommerce.users.repository;

import com.ecommerce.users.model.ApplicationStatus;
import com.ecommerce.users.model.SellerApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Long> {
    List<SellerApplication> findByStatus(ApplicationStatus status);
    Optional<SellerApplication> findByUserIdAndStatus(Long userId, ApplicationStatus status);
}