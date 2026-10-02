package com.ecommerce.orders.service;

import com.ecommerce.orders.model.Order;
import com.ecommerce.orders.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);

    private final JavaMailSender mailSender;
    private final String adminEmail;

    public OrderNotificationService(JavaMailSender mailSender,
                                    @Value("${app.notifications.admin-email}") String adminEmail) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
    }

    // A failed notification must never fail the checkout itself — the order is already real and paid-for
    // by the time this runs, so any email problem is logged and swallowed, not thrown.
    public void notifyNewOrder(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("New order #" + order.getId() + " — " + order.getPaymentMethod());
            message.setText(buildBody(order));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Could not send order notification email for order {}: {}", order.getId(), e.getMessage());
        }
    }

     
public void notifyCustomer(Order order, String customerEmail) {
    if (customerEmail == null || customerEmail.isBlank()) return;
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customerEmail);
        message.setSubject("We received your order #" + order.getId());
        message.setText("""
            Hi %s,

            Thanks for your order! We've received it and will contact you shortly to confirm delivery details.

            Order #%d
            Total: $%s
            Payment method: %s

            We'll be in touch soon.
            """.formatted(order.getShippingAddress().getFullName(), order.getId(),
                order.getTotalAmount(), order.getPaymentMethod()));
        mailSender.send(message);
    } catch (Exception e) {
        log.warn("Could not send customer confirmation email for order {}: {}", order.getId(), e.getMessage());
    }
}
    private String buildBody(Order order) {
        String itemLines = order.getItems().stream()
            .map(OrderItem::toLine)
            .collect(Collectors.joining("\n"));

        return """
            New order placed.

            Order #%d
            Payment method: %s
            Total: $%s (includes $%s shipping)

            Customer: %s
            Phone: %s
            Address: %s, %s, %s
            Notes: %s

            Items:
            %s
            """.formatted(
                order.getId(), order.getPaymentMethod(), order.getTotalAmount(), order.getShippingCost(),
                order.getShippingAddress().getFullName(), order.getShippingAddress().getPhone(),
                order.getShippingAddress().getAddressLine(), order.getShippingAddress().getCity(),
                order.getShippingAddress().getCountry(),
                order.getShippingAddress().getNotes() == null ? "—" : order.getShippingAddress().getNotes(),
                itemLines
            );
    }
}