package com.ecommerce.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerEntryResponse(
    Long id,
    LedgerType type,
    String productName,
    Integer quantity,
    BigDecimal unitAmount,
    LocalDate entryDate
) {}
