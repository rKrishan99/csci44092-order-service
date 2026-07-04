package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.ProductResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set the product service URL manually since @Value won't inject in unit tests
        orderService = new OrderService();
        // Use reflection or a setter if needed — see note below
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("CUST-01");
        request.setProductId(1L);
        request.setQuantity(3);

        ProductResponse mockProduct = new ProductResponse();
        mockProduct.setProductId(1L);
        mockProduct.setName("Laptop");
        mockProduct.setUnitPrice(1000.0);

        Order savedOrder = new Order("CUST-01", 1L, "Laptop", 3, 3000.0, null);
        savedOrder.setOrderId(1L);

        when(restTemplate.getForObject(anyString(), eq(ProductResponse.class)))
                .thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals(3000.0, result.getTotalPrice());
        assertEquals("Laptop", result.getProductName());
        verify(orderRepository, times(1)).save(any(Order.class));
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
}