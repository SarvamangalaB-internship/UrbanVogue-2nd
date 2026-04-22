package com.urbanvogue.order.service;

import com.urbanvogue.order.dto.OrderRequest;
import com.urbanvogue.order.model.Order;
import com.urbanvogue.order.model.OrderItem;
import com.urbanvogue.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    // Reads from application.properties
    @Value("${product.service.url}")
    private String productServiceUrl;

    // ─────────────────────────────────────────
    // CREATE: Place a new order
    // ─────────────────────────────────────────
    public Order placeOrder(OrderRequest request) {

        // ── BUSINESS RULE 1: Username cannot be empty ──
        if (request.getCustomerUsername() == null ||
                request.getCustomerUsername().trim().isEmpty()) {
            throw new RuntimeException("Customer username is required.");
        }

        // ── BUSINESS RULE 2: Order must have at least one item ──
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item.");
        }

        // ── BUSINESS RULE 3: Validate each item ──
        for (OrderRequest.OrderItemRequest item : request.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new RuntimeException(
                        "Quantity for product ID " + item.getProductId() +
                                " must be greater than zero."
                );
            }
            if (item.getPriceAtPurchase() <= 0) {
                throw new RuntimeException(
                        "Price for product ID " + item.getProductId() +
                                " must be greater than zero."
                );
            }

            // ── BUSINESS RULE 4: Verify product exists in Product Service ──
            // This is a real inter-service call using RestTemplate
            // It calls YOUR Product Service GET /api/products/{id}
            try {
                String url = productServiceUrl + "/api/products/" + item.getProductId();
                restTemplate.getForObject(url, Object.class);
                // If product doesn't exist, Product Service returns 404
                // RestTemplate throws exception → we catch it below
            } catch (Exception e) {
                throw new RuntimeException(
                        "Product ID " + item.getProductId() +
                                " does not exist in Product Service. " +
                                "Please add it first via POST /api/products"
                );
            }
        }

        // ── STEP 1: Convert DTO items to OrderItem entities ──
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemReq -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemReq.getProductId());
                    item.setProductName(itemReq.getProductName());
                    item.setQuantity(itemReq.getQuantity());
                    item.setPriceAtPurchase(itemReq.getPriceAtPurchase());
                    return item;
                })
                .collect(Collectors.toList());

        // ── STEP 2: Auto-calculate total ──
        // Total = sum of (price × quantity) for each item
        double total = orderItems.stream()
                .mapToDouble(item ->
                        item.getPriceAtPurchase() * item.getQuantity()
                )
                .sum();

        // ── STEP 3: Build the Order ──
        Order order = new Order();
        order.setCustomerUsername(request.getCustomerUsername());
        order.setStatus("PENDING");
        order.setTotalAmount(Math.round(total * 100.0) / 100.0); // Round to 2 decimals
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(orderItems);

        // ── STEP 4: Link items back to order ──
        // Required for @OneToMany bidirectional relationship
        orderItems.forEach(item -> item.setOrder(order));

        // ── STEP 5: Save (CascadeType.ALL saves items too) ──
        return orderRepository.save(order);
    }

    // ─────────────────────────────────────────
    // READ: Get all orders
    // ─────────────────────────────────────────
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ─────────────────────────────────────────
    // READ: Get one order by ID
    // ─────────────────────────────────────────
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Order not found with id: " + id
                ));
    }

    // ─────────────────────────────────────────
    // READ: All orders for a customer
    // ─────────────────────────────────────────
    public List<Order> getOrdersByCustomer(String username) {
        List<Order> orders = orderRepository.findByCustomerUsername(username);
        if (orders.isEmpty()) {
            throw new RuntimeException(
                    "No orders found for customer: " + username
            );
        }
        return orders;
    }

    // ─────────────────────────────────────────
    // UPDATE: Change order status
    // Only valid transitions are allowed
    // ─────────────────────────────────────────
    public Order updateOrderStatus(Long id, String newStatus) {

        // Validate status is a known value
        List<String> validStatuses = List.of(
                "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"
        );

        if (!validStatuses.contains(newStatus.toUpperCase())) {
            throw new RuntimeException(
                    "Invalid status: '" + newStatus + "'. " +
                            "Valid values: " + validStatuses
            );
        }

        Order order = getOrderById(id);
        String currentStatus = order.getStatus();

        // ── BUSINESS RULE: Status can only move forward ──
        // DELIVERED orders cannot be changed
        if (currentStatus.equals("DELIVERED")) {
            throw new RuntimeException(
                    "Cannot change status. Order #" + id +
                            " is already DELIVERED."
            );
        }

        // CANCELLED orders cannot be reactivated
        if (currentStatus.equals("CANCELLED")) {
            throw new RuntimeException(
                    "Cannot change status. Order #" + id +
                            " is already CANCELLED."
            );
        }

        order.setStatus(newStatus.toUpperCase());
        return orderRepository.save(order);
    }

    // ─────────────────────────────────────────
    // DELETE: Cancel order (PENDING only)
    // ─────────────────────────────────────────
    public String cancelOrder(Long id) {
        Order order = getOrderById(id);

        // ── BUSINESS RULE: Only PENDING orders can be cancelled ──
        if (!order.getStatus().equals("PENDING")) {
            throw new RuntimeException(
                    "Cannot cancel Order #" + id +
                            ". Current status: " + order.getStatus() +
                            ". Only PENDING orders can be cancelled."
            );
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return "Order #" + id + " has been successfully cancelled.";
    }
}