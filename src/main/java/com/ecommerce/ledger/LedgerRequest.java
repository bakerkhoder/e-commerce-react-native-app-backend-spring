package com.ecommerce.ledger;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerRequest(
    @NotNull LedgerType type,
    @NotNull Long productId,
    @NotNull @Min(1) @Max(100000) Integer quantity,
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal unitAmount,
    LocalDate entryDate,
    @Size(max = 300) String notes,
    boolean addToStock
) {}
