package com.ecommerce.orders.service;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.orders.dto.CheckoutRequest;
import com.ecommerce.orders.model.Order;
import com.ecommerce.orders.model.OrderItem;
import com.ecommerce.orders.model.OrderStatus;
import com.ecommerce.orders.model.ShippingAddress;
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
    private final OrderNotificationService notificationService;
    private final com.ecommerce.users.service.UserService userService;

    public OrderService(OrderRepository orderRepository, CartService cartService, ProductService productService,
            OrderNotificationService notificationService, com.ecommerce.users.service.UserService userService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @Transactional
    public Order checkout(Long userId, CheckoutRequest req) {
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        for (CartItem item : cart.getItems()) {
            if (!productService.hasStock(item.getProductId(), item.getQuantity())) {
                throw new IllegalStateException("Insufficient stock for " + item.getProductName());
            }
        }

        ShippingAddress address = new ShippingAddress();
        address.setFullName(req.fullName());
        address.setPhone(req.phone());
        address.setCity(req.city());
        address.setAddressLine(req.addressLine());
        address.setNotes(req.notes());

        Order order = new Order(userId);
        order.setShippingAddress(address);
        order.setShippingMethod(req.shippingMethod());
        order.setShippingCost(req.shippingMethod().getCost());
        order.setPaymentMethod(req.paymentMethod());

        BigDecimal itemsTotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);
            itemsTotal = itemsTotal.add(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            productService.reduceStock(cartItem.getProductId(), cartItem.getQuantity());
        }

        order.setTotalAmount(itemsTotal.add(order.getShippingCost()));
        Order saved = orderRepository.save(order);
        cartService.clearCart(userId);

        if (req.saveAsDefault()) {
            userService.updateDefaultAddress(userId, req.phone(), req.city(), req.addressLine());
        }

        notificationService.notifyNewOrder(saved); // best-effort, never blocks the order itself
        com.ecommerce.users.model.User customer = userService.getById(userId);
        notificationService.notifyCustomer(saved, customer.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
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