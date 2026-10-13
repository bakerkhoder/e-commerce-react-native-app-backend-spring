package com.ecommerce.analytics;

import jakarta.persistence.*;
import java.time.LocalDate;

/** One row per product per day, so a busy page costs one UPDATE instead of one row per view. */
@Entity
@Table(name = "product_daily_views",
       uniqueConstraints = @UniqueConstraint(columnNames = { "product_id", "view_date" }))
public class ProductDailyViews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "view_date")
    private LocalDate viewDate;

    private long views;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDate getViewDate() {
        return viewDate;
    }

    public void setViewDate(LocalDate viewDate) {
        this.viewDate = viewDate;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }
}
