package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Case-insensitive substring match on name, category and description.
    // Name matches rank first (prefix, then contains), then everything else.
    @Query("""
        select p from Product p
        left join fetch p.category c
        where lower(p.name) like lower(concat('%', :term, '%'))
           or lower(c.name) like lower(concat('%', :term, '%'))
           or lower(p.description) like lower(concat('%', :term, '%'))
        order by
          case when lower(p.name) like lower(concat(:term, '%')) then 0
               when lower(p.name) like lower(concat('%', :term, '%')) then 1
               else 2 end,
          p.name
        """)
    List<Product> searchByKeyword(@Param("term") String term, Pageable pageable);
}