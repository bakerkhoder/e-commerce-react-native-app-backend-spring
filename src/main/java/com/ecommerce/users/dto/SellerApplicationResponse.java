package com.ecommerce.users.dto;

import com.ecommerce.users.model.SellerApplication;
import java.time.Instant;

public record SellerApplicationResponse(Long id, Long userId, String businessName, String status, Instant submittedAt) {
    public static SellerApplicationResponse from(SellerApplication a) {
        return new SellerApplicationResponse(a.getId(), a.getUserId(), a.getBusinessName(), a.getStatus().name(), a.getSubmittedAt());
    }
}