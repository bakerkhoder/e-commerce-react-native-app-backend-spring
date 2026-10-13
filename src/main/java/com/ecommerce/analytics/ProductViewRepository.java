package com.ecommerce.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ProductViewRepository extends JpaRepository<ProductDailyViews, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO product_daily_views (product_id, view_date, views) VALUES (:pid, :day, 1) "
            + "ON CONFLICT (product_id, view_date) DO UPDATE SET views = product_daily_views.views + 1", nativeQuery = true)
    void increment(@Param("pid") Long productId, @Param("day") LocalDate day);

    @Query("select v.productId, sum(v.views) from ProductDailyViews v where v.viewDate >= :from group by v.productId")
    List<Object[]> totalsSince(@Param("from") LocalDate from);
}
