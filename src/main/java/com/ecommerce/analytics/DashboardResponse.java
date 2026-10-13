package com.ecommerce.analytics;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    int days,
    Kpis kpis,
    List<Point> series,
    List<ProductStat> topProducts,
    List<ViewStat> mostViewed,
    List<StockStat> lowStock,
    int productsMissingCost,
    List<Insight> insights
) {
    public record Kpis(
        BigDecimal revenue,
        BigDecimal cost,
        BigDecimal profit,
        double marginPct,
        BigDecimal spentOnStock,
        BigDecimal netCashFlow,
        int orders,
        int unitsSold
    ) {}

    public record Point(String label, BigDecimal revenue, BigDecimal profit) {}

    public record ProductStat(Long productId, String name, int units, BigDecimal revenue, BigDecimal profit) {}

    public record ViewStat(Long productId, String name, long views, int unitsSold, double conversionPct) {}

    public record StockStat(Long productId, String name, int stock) {}

    public record Insight(String severity, String title, String message, Long productId) {}
}
