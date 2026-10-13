package com.ecommerce.analytics;

import com.ecommerce.common.SecurityUtils;
import com.ecommerce.ledger.LedgerEntry;
import com.ecommerce.ledger.LedgerEntryResponse;
import com.ecommerce.ledger.LedgerRequest;
import com.ecommerce.ledger.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final DashboardService dashboardService;
    private final DashboardAiService aiService;
    private final ProductViewService viewService;
    private final LedgerService ledgerService;

    public AnalyticsController(DashboardService dashboardService, DashboardAiService aiService,
            ProductViewService viewService, LedgerService ledgerService) {
        this.dashboardService = dashboardService;
        this.aiService = aiService;
        this.viewService = viewService;
        this.ledgerService = ledgerService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal Long userId, Authentication auth) {
        return dashboardService.build(scope(userId, auth), clamp(days));
    }

    @PostMapping("/dashboard/ai-plan")
    public Map<String, String> aiPlan(@RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal Long userId, Authentication auth) {
        DashboardResponse data = dashboardService.build(scope(userId, auth), clamp(days));
        try {
            return Map.of("plan", aiService.plan(data));
        } catch (Exception e) {
            // The deterministic insights are still on screen, so fail softly with a hint.
            throw new IllegalStateException(
                    "The AI advisor is unavailable right now. The insights above still apply.");
        }
    }

    @PostMapping("/ledger")
    public LedgerEntryResponse record(@Valid @RequestBody LedgerRequest request,
            @AuthenticationPrincipal Long userId, Authentication auth) {
        LedgerEntry e = ledgerService.record(userId, SecurityUtils.isAdmin(auth), request);
        return new LedgerEntryResponse(e.getId(), e.getType(), e.getProductName(), e.getQuantity(),
                e.getUnitAmount(), e.getEntryDate());
    }

    @PostMapping("/analytics/views/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordView(@PathVariable Long productId) {
        viewService.record(productId);
    }

    /** A seller is scoped to their own products; an admin sees the whole shop. */
    private Long scope(Long userId, Authentication auth) {
        return SecurityUtils.isAdmin(auth) ? null : userId;
    }

    private int clamp(int days) {
        return Math.min(Math.max(days, 1), 365);
    }
}
