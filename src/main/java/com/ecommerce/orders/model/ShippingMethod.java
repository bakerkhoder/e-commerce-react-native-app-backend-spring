package com.ecommerce.orders.model;

import java.math.BigDecimal;

public enum ShippingMethod {
    INSIDE_BEIRUT(new BigDecimal("5.00")),
    OUTSIDE_BEIRUT(new BigDecimal("7.00"));

    private final BigDecimal cost;
    ShippingMethod(BigDecimal cost) { this.cost = cost; }
    public BigDecimal getCost() { return cost; } // server decides the price — never trust a client-supplied cost
}