package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.ProductResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
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

    // This URL will come from application.properties or environment variable
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

        // Step 4: (Member 4 will plug in RabbitMQ publishing here)

        return savedOrder;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }
}