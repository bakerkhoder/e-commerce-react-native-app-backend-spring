package com.ecommerce.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByEntryDateGreaterThanEqual(LocalDate from);

    List<LedgerEntry> findBySellerIdAndEntryDateGreaterThanEqual(Long sellerId, LocalDate from);
}
