package com.ecommerce.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP request throttle for the endpoints that are cheap to abuse: auth (password
 * guessing), guest checkout (it emails whatever address it is handed, so it doubles as
 * an email bomb), and search/assistant (each call triggers a paid model request).
 *
 * <p>In-memory and per instance, which is correct for a single server. Behind a reverse
 * proxy every client looks like the proxy unless you set
 * {@code server.forward-headers-strategy=framework} — only do that for a proxy you own.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(String prefix, String method, int max, long windowMs) {}

    private static final List<Rule> RULES = List.of(
            new Rule("/api/auth/", "POST", 10, 60_000),
            new Rule("/api/orders/guest-checkout", "POST", 5, 3_600_000),
            new Rule("/api/assistant/", "GET", 30, 60_000),
            new Rule("/api/search", "GET", 60, 60_000),
            // the view ping is public, so it needs its own ceiling
            new Rule("/api/analytics/views/", "POST", 120, 60_000),
            // each AI plan costs a model call
            new Rule("/api/dashboard/ai-plan", "POST", 5, 60_000));

    private static final class Window {
        long start;
        int count;
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Rule rule = RULES.stream()
                .filter(r -> r.method().equals(req.getMethod()) && req.getRequestURI().startsWith(r.prefix()))
                .findFirst().orElse(null);

        if (rule != null && !allow(req.getRemoteAddr() + "|" + rule.prefix(), rule)) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"message\":\"Too many requests. Please try again shortly.\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private boolean allow(String key, Rule rule) {
        long now = System.currentTimeMillis();
        // Bound memory: drop windows nobody has touched in the last hour.
        if (windows.size() > 10_000) {
            windows.values().removeIf(w -> now - w.start > 3_600_000);
        }
        Window w = windows.computeIfAbsent(key, k -> new Window());
        synchronized (w) {
            if (now - w.start > rule.windowMs()) {
                w.start = now;
                w.count = 0;
            }
            return ++w.count <= rule.max();
        }
    }
}
