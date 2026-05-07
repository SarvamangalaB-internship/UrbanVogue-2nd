package com.urbanvogue.order_service.service;

import com.urbanvogue.order_service.dto.*;
import com.urbanvogue.order_service.model.*;
import com.urbanvogue.order_service.repository.OrderRepository;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.of;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;


    // PLACE ORDER

    public Order placeOrder(OrderRequest request) {

        if (request.getCustomerUsername() == null) {
            throw new RuntimeException("Username required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Items required");
        }

        //  Convert items
        List<OrderItem> items = request.getItems().stream()
                .map(i -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(i.getProductId());
                    item.setProductName(i.getProductName());
                    item.setQuantity(i.getQuantity());
                    item.setPriceAtPurchase(i.getPriceAtPurchase());
                    return item;
                }).collect(Collectors.toList());

        double total = items.stream()
                .mapToDouble(i -> i.getPriceAtPurchase() * i.getQuantity())
                .sum();

        // Create Order
        Order order = new Order();
        order.setCustomerUsername(request.getCustomerUsername());
        order.setItems(items);
        order.setTotalAmount(total);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus("PLACED");

        items.forEach(i -> i.setOrder(order));

        //  SAVE FIRST (VERY IMPORTANT)
        Order savedOrder = orderRepository.save(order);

        String paymentStatus = "FAILED";
        String txnId = "FAILED";

        //  PAYMENT LOGIC

        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {

            paymentStatus = "PENDING";
            txnId = "NA";

        } else if ("UPI".equalsIgnoreCase(request.getPaymentMethod())) {

            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("orderId", savedOrder.getId());
                requestBody.put("customerUsername", request.getCustomerUsername());
                requestBody.put("amount", total);
                requestBody.put("paymentMethod", "UPI");

                Map response = restTemplate.postForObject(
                        "http://localhost:8084/api/payments/initiate",
                        requestBody,
                        Map.class
                );

                paymentStatus = (String) response.get("status");
                txnId = (String) response.get("transactionId");

            } catch (Exception e) {
                throw new RuntimeException("Payment service error");
            }
        }

        // 🔹 Update order with payment result
        savedOrder.setPaymentStatus(paymentStatus);
        savedOrder.setTransactionId(txnId);

        Order finalOrder = orderRepository.save(savedOrder);

        // NOTIFICATION

        try {
            restTemplate.postForObject(
                    "http://localhost:8085/api/notifications/order-placed" +
                            "?username=" + finalOrder.getCustomerUsername() +
                            "&orderId=" + finalOrder.getId() +
                            "&amount=" + finalOrder.getTotalAmount(),
                    null,
                    String.class
            );
        } catch (Exception e) {
            System.out.println("Notification failed");
        }

        return finalOrder;
    }


    //  GET ALL

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }


    //  GET BY ID

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // HISTORY

    public List<Order> getOrdersByCustomer(String username) {
        List<Order> orders = orderRepository.findByCustomerUsername(username);
        orders.sort((o1, o2) -> Long.compare(o2.getId(), o1.getId()));
        return orders;
    }


    //  UPDATE STATUS

    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status.toUpperCase());
        return orderRepository.save(order);
    }

    //  CANCEL ORDER

    public String cancelOrder(Long id) {

        Order order = getOrderById(id);

        if ("CANCELLED".equals(order.getStatus())) {
            return "Already cancelled";
        }

        //  REFUND logic (only for UPI and successful payments)
        if ("UPI".equalsIgnoreCase(order.getPaymentMethod())
                && "SUCCESS".equals(order.getPaymentStatus())) {

            try {
                restTemplate.postForObject(
                        "http://localhost:8084/api/payments/refund/" +
                                order.getTransactionId(),
                        null,
                        String.class
                );
            } catch (Exception e) {
                System.out.println("Refund failed");
            }
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        // Notification
        try {
            restTemplate.postForObject(
                    "http://localhost:8085/api/notifications/order-cancelled" +
                            "?username=" + order.getCustomerUsername() +
                            "&orderId=" + order.getId(),
                    null,
                    String.class
            );
        } catch (Exception e) {
            System.out.println("Notification failed");
        }

        return "Order cancelled successfully";
    }
}