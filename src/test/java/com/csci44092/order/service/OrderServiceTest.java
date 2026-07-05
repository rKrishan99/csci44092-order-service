package com.csci44092.order.service;

import com.csci44092.order.dto.CreateOrderRequest;
import com.csci44092.order.dto.ProductResponse;
import com.csci44092.order.entity.Order;
import com.csci44092.order.messaging.OrderEvent;
import com.csci44092.order.messaging.OrderEventProducer;
import com.csci44092.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("CUST-01");
        request.setProductId(1L);
        request.setQuantity(3);

        ProductResponse mockProduct = new ProductResponse();
        mockProduct.setProductId(1L);
        mockProduct.setName("Laptop");
        mockProduct.setUnitPrice(1000.0);

        Order savedOrder = new Order("CUST-01", 1L, "Laptop", 3, 3000.0, LocalDateTime.now());
        savedOrder.setOrderId(1L);

        when(restTemplate.getForObject(anyString(), eq(ProductResponse.class)))
                .thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        doNothing().when(orderEventProducer).publishOrderEvent(any(OrderEvent.class));

        Order result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(3000.0, result.getTotalPrice());
        assertEquals("Laptop", result.getProductName());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventProducer, times(1)).publishOrderEvent(any(OrderEvent.class));
    }

    @Test
    void testCreateOrder_ProductNotFound() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("CUST-01");
        request.setProductId(99L);
        request.setQuantity(2);

        when(restTemplate.getForObject(anyString(), eq(ProductResponse.class)))
                .thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(request));

        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    void testGetOrderById_Success() {
        Order mockOrder = new Order("CUST-01", 1L, "Laptop", 2, 2000.0, LocalDateTime.now());
        mockOrder.setOrderId(1L);

        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(mockOrder));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getOrderById(99L));

        assertTrue(exception.getMessage().contains("Order not found"));
    }
}