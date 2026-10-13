package com.ecommerce.analytics;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.ledger.LedgerEntry;
import com.ecommerce.ledger.LedgerService;
import com.ecommerce.ledger.LedgerType;
import com.ecommerce.orders.model.Order;
import com.ecommerce.orders.model.OrderItem;
import com.ecommerce.orders.model.OrderStatus;
import com.ecommerce.orders.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DashboardService {

    /** Orders that represent money actually earned. PENDING and CANCELLED do not count. */
    private static final Set<OrderStatus> COUNTED = Set.of(
            OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final OrderService orderService;
    private final LedgerService ledgerService;
    private final ProductService productService;
    private final ProductViewService viewService;

    public DashboardService(OrderService orderService, LedgerService ledgerService,
            ProductService productService, ProductViewService viewService) {
        this.orderService = orderService;
        this.ledgerService = ledgerService;
        this.productService = productService;
        this.viewService = viewService;
    }

    /** One normalized sale, whether it came from an online order or a manually recorded sale. */
    private record Line(LocalDate date, Long productId, String name, int qty, BigDecimal price, BigDecimal cost) {
        BigDecimal revenue() {
            return price.multiply(BigDecimal.valueOf(qty));
        }

        BigDecimal cogs() {
            return (cost == null ? BigDecimal.ZERO : cost).multiply(BigDecimal.valueOf(qty));
        }
    }

    private static BigDecimal sum(Collection<Line> lines, Function<Line, BigDecimal> f) {
        return lines.stream().map(f).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static double pct(BigDecimal part, BigDecimal whole) {
        return whole.signum() == 0 ? 0
                : part.multiply(HUNDRED).divide(whole, 1, RoundingMode.HALF_UP).doubleValue();
    }

    @Transactional(readOnly = true)
    public DashboardResponse build(Long sellerScope, int days) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate from = LocalDate.now().minusDays(days - 1L);

        // 1) Every sale line: paid online orders + manually recorded sales
        List<Line> lines = new ArrayList<>();
        Set<Long> orderIds = new HashSet<>();
        for (Order o : orderService.getOrdersSince(from.atStartOfDay(zone).toInstant(), COUNTED)) {
            for (OrderItem i : o.getItems()) {
                if (sellerScope != null && !sellerScope.equals(i.getSellerId())) {
                    continue; // a seller only ever sees their own lines
                }
                orderIds.add(o.getId());
                lines.add(new Line(o.getCreatedAt().atZone(zone).toLocalDate(), i.getProductId(),
                        i.getProductName(), i.getQuantity(), i.getUnitPrice(), i.getUnitCost()));
            }
        }

        BigDecimal spent = BigDecimal.ZERO;
        for (LedgerEntry e : ledgerService.since(sellerScope, from)) {
            if (e.getType() == LedgerType.PURCHASE) {
                spent = spent.add(e.getUnitAmount().multiply(BigDecimal.valueOf(e.getQuantity())));
            } else {
                lines.add(new Line(e.getEntryDate(), e.getProductId(), e.getProductName(),
                        e.getQuantity(), e.getUnitAmount(), e.getUnitCost()));
            }
        }

        // 2) KPIs
        BigDecimal revenue = sum(lines, Line::revenue);
        BigDecimal cogs = sum(lines, Line::cogs);
        BigDecimal profit = revenue.subtract(cogs);
        var kpis = new DashboardResponse.Kpis(revenue, cogs, profit, pct(profit, revenue), spent,
                revenue.subtract(spent), orderIds.size(), lines.stream().mapToInt(Line::qty).sum());

        // 3) Chart series: daily up to 31 days, weekly up to 100, otherwise 30-day buckets
        int bucket = days <= 31 ? 1 : days <= 100 ? 7 : 30;
        int n = (int) Math.ceil(days / (double) bucket);
        BigDecimal[] rev = new BigDecimal[n];
        BigDecimal[] prof = new BigDecimal[n];
        Arrays.fill(rev, BigDecimal.ZERO);
        Arrays.fill(prof, BigDecimal.ZERO);
        for (Line l : lines) {
            int idx = (int) (ChronoUnit.DAYS.between(from, l.date()) / bucket);
            if (idx >= 0 && idx < n) {
                rev[idx] = rev[idx].add(l.revenue());
                prof[idx] = prof[idx].add(l.revenue().subtract(l.cogs()));
            }
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<DashboardResponse.Point> series = IntStream.range(0, n)
                .mapToObj(i -> new DashboardResponse.Point(
                        from.plusDays((long) i * bucket).format(fmt), rev[i], prof[i]))
                .toList();

        // 4) Per-product breakdowns, limited to the products this user may see
        List<Product> products = sellerScope == null
                ? productService.getAllIncludingUnapproved()
                : productService.getMyProducts(sellerScope);
        Map<Long, Product> byId = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        Map<Long, List<Line>> byProduct = lines.stream().collect(Collectors.groupingBy(Line::productId));
        Map<Long, Long> views = new HashMap<>(viewService.viewsSince(from));
        views.keySet().retainAll(byId.keySet());

        var top = byProduct.entrySet().stream()
                .filter(e -> byId.containsKey(e.getKey()))
                .map(e -> {
                    BigDecimal r = sum(e.getValue(), Line::revenue);
                    return new DashboardResponse.ProductStat(e.getKey(),
                            e.getValue().get(0).name(),
                            e.getValue().stream().mapToInt(Line::qty).sum(),
                            r, r.subtract(sum(e.getValue(), Line::cogs)));
                })
                .sorted(Comparator.comparing(DashboardResponse.ProductStat::revenue).reversed())
                .limit(5)
                .toList();

        var mostViewed = views.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    int sold = byProduct.getOrDefault(e.getKey(), List.of()).stream()
                            .mapToInt(Line::qty).sum();
                    Product p = byId.get(e.getKey());
                    return new DashboardResponse.ViewStat(e.getKey(), p == null ? "—" : p.getName(),
                            e.getValue(), sold,
                            e.getValue() == 0 ? 0 : Math.round(sold * 1000.0 / e.getValue()) / 10.0);
                })
                .toList();

        var lowStock = products.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 5)
                .sorted(Comparator.comparingInt(Product::getStockQuantity))
                .limit(5)
                .map(p -> new DashboardResponse.StockStat(p.getId(), p.getName(), p.getStockQuantity()))
                .toList();

        int missingCost = (int) lines.stream()
                .filter(l -> l.cost() == null)
                .map(Line::productId)
                .distinct()
                .count();

        return new DashboardResponse(days, kpis, series, top, mostViewed, lowStock, missingCost,
                insights(products, byProduct, views, days, missingCost));
    }

    /**
     * Deterministic advice rules. Unlike the AI plan these are reproducible and auditable,
     * which is why they are the dependable half of the dashboard.
     */
    private List<DashboardResponse.Insight> insights(List<Product> products, Map<Long, List<Line>> byProduct,
            Map<Long, Long> views, int days, int missingCost) {
        List<DashboardResponse.Insight> out = new ArrayList<>();

        for (Product p : products) {
            List<Line> ls = byProduct.getOrDefault(p.getId(), List.of());
            int sold = ls.stream().mapToInt(Line::qty).sum();
            long v = views.getOrDefault(p.getId(), 0L);
            int stock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
            String name = p.getName();

            if (v >= 20 && sold * 100.0 / v < 5) {
                out.add(insight("OPPORTUNITY", "High interest, low sales: " + name,
                        v + " views but only " + sold
                                + " sold. Try a 10-15% discount, better photos or a clearer description.",
                        p.getId()));
            }
            if (sold >= 5) {
                double daysLeft = stock / (sold / (double) days);
                if (daysLeft < 7) {
                    out.add(insight("WARNING", "Restock soon: " + name,
                            "About " + Math.round(daysLeft) + " days of stock left at the current pace.",
                            p.getId()));
                }
            }
            if (days >= 30 && sold == 0 && stock >= 20) {
                out.add(insight("OPPORTUNITY", "Idle stock: " + name,
                        stock + " units and no sales in " + days + " days. Consider a clearance price or a bundle.",
                        p.getId()));
            }
            BigDecimal rev = sum(ls, Line::revenue);
            if (rev.signum() > 0 && ls.stream().noneMatch(l -> l.cost() == null)) {
                double margin = pct(rev.subtract(sum(ls, Line::cogs)), rev);
                if (margin < 15) {
                    out.add(insight("WARNING", "Thin margin: " + name,
                            "Margin is only " + margin + "%. Raise the price or negotiate a lower cost.",
                            p.getId()));
                }
            }
        }

        if (missingCost > 0) {
            out.add(insight("INFO", "Profit may be overstated",
                    missingCost + " product(s) sold without a cost price. Record a purchase for them "
                            + "so profit is accurate.",
                    null));
        }

        out.sort(Comparator.comparingInt(i -> List.of("WARNING", "OPPORTUNITY", "INFO").indexOf(i.severity())));
        return out.size() > 8 ? new ArrayList<>(out.subList(0, 8)) : out;
    }

    private static DashboardResponse.Insight insight(String severity, String title, String message, Long productId) {
        return new DashboardResponse.Insight(severity, title, message, productId);
    }
}
