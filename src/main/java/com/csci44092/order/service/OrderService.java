package com.csci44092.order.service;

import com.csci44092.order.dto.CreateOrderRequest;
import com.csci44092.order.dto.ProductResponse;
import com.csci44092.order.entity.Order;
import com.csci44092.order.messaging.OrderEvent;
import com.csci44092.order.messaging.OrderEventProducer;
import com.csci44092.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderEventProducer orderEventProducer;

    @Value("${product.service.url:http://localhost:8081}")
    private String productServiceUrl;

    public Order createOrder(CreateOrderRequest request) {
        // Step 1: Fetch product details from Product Service
        String url = productServiceUrl + "/products/" + request.getProductId();
        ProductResponse product = restTemplate.getForObject(url, ProductResponse.class);

        if (product == null) {
            throw new RuntimeException("Product not found with ID: " + request.getProductId());
        }

        // Step 2: Calculate total price
        Double totalPrice = product.getUnitPrice() * request.getQuantity();

        // Step 3: Build and save the order
        Order order = new Order(
                request.getCustomerId(),
                request.getProductId(),
                product.getName(),
                request.getQuantity(),
                totalPrice,
                LocalDateTime.now()
        );

        Order savedOrder = orderRepository.save(order);

        // Step 4: Publish OrderEvent to RabbitMQ
        OrderEvent event = new OrderEvent(
                savedOrder.getOrderId(),
                savedOrder.getCustomerId(),
                LocalDateTime.now()
        );
        orderEventProducer.publishOrderEvent(event);

        return savedOrder;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }
}