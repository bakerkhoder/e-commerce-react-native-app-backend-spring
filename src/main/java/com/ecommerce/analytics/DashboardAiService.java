package com.ecommerce.analytics;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Turns the already-computed dashboard figures into a short plain-language plan.
 *
 * The model is given numbers and told to invent nothing: every claim it makes has to be
 * traceable to something this response already contains. The rule-based insights remain
 * the dependable part.
 */
@Service
public class DashboardAiService {

    private final ChatClient chatClient;

    public DashboardAiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String plan(DashboardResponse d) {
        var k = d.kpis();
        String facts = """
                Period: last %d days
                Revenue %s, profit %s (margin %s%%), spent on stock %s
                Orders %d, units sold %d
                Top products: %s
                Most viewed (views / sold): %s
                Low stock: %s
                Findings: %s
                """.formatted(d.days(), k.revenue(), k.profit(), k.marginPct(), k.spentOnStock(),
                k.orders(), k.unitsSold(),
                join(d.topProducts().stream().map(p -> p.name() + " (" + p.units() + " sold)")),
                join(d.mostViewed().stream().map(v -> v.name() + " (" + v.views() + " / " + v.unitsSold() + ")")),
                join(d.lowStock().stream().map(s -> s.name() + " (" + s.stock() + " left)")),
                join(d.insights().stream().map(DashboardResponse.Insight::title)));

        return chatClient.prompt()
                .system("You are a concise retail advisor. Use ONLY the figures provided and never invent numbers. "
                        + "Reply with at most 6 short bullet points, each an action the shop owner can take this week.")
                .user(facts)
                .call()
                .content();
    }

    private static String join(Stream<String> s) {
        String r = s.collect(Collectors.joining("; "));
        return r.isEmpty() ? "none" : r;
    }
}
