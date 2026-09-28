package com.ecommerce.orders.service;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.orders.model.Order;
import com.ecommerce.orders.model.OrderItem;
import com.ecommerce.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import com.ecommerce.common.NotFoundException;
import java.util.List;
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, CartService cartService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
    }

    @Transactional
    public Order checkout(Long userId) {
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        // Re-validate stock at checkout time, not just at add-to-cart time —
        // stock can change between adding an item and actually paying for it
        for (CartItem item : cart.getItems()) {
            if (!productService.hasStock(item.getProductId(), item.getQuantity())) {
                throw new IllegalStateException("Insufficient stock for " + item.getProductName());
            }
        }

        Order order = new Order(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);

            total = total.add(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            productService.reduceStock(cartItem.getProductId(), cartItem.getQuantity());
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        cartService.clearCart(userId); // we need to add this method to CartService

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Order getOrderForUser(Long userId, Long orderId) {
        // Lookup includes the user id: someone else's order is indistinguishable from a
        // missing one
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }
}