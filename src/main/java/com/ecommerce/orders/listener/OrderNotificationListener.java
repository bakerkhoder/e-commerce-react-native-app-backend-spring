package com.ecommerce.orders.listener;

import com.ecommerce.orders.event.OrderPlacedEvent;
import com.ecommerce.orders.service.OrderNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderNotificationListener {

    private final OrderNotificationService notificationService;

    public OrderNotificationListener(OrderNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        notificationService.notifyNewOrder(event.order());
        notificationService.notifyCustomer(event.order(), event.customerEmail());
    }
}