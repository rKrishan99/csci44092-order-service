package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateOrder_ReturnsCreated() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("CUST-01");
        request.setProductId(1L);
        request.setQuantity(2);

        Order mockOrder = new Order("CUST-01", 1L, "Laptop", 2, 2000.0, LocalDateTime.now());
        mockOrder.setOrderId(1L);

        Mockito.when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(mockOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.totalPrice").value(2000.0));
    }

    @Test
    void testGetOrder_ReturnsOk() throws Exception {
        Order mockOrder = new Order("CUST-01", 1L, "Laptop", 2, 2000.0, LocalDateTime.now());
        mockOrder.setOrderId(1L);

        Mockito.when(orderService.getOrderById(1L)).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("CUST-01"));
    }
}