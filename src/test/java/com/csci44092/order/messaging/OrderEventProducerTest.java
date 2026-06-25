package com.csci44092.order.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderEventProducer}.
 * Uses Mockito to mock RabbitTemplate so no live broker is needed.
 */
@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderEventProducer orderEventProducer;

    private static final String TEST_EXCHANGE = "order.exchange";
    private static final String TEST_ROUTING_KEY = "order.created";

    @BeforeEach
    void setUp() {
        // Inject @Value fields manually since we are not loading the Spring context
        ReflectionTestUtils.setField(orderEventProducer, "exchangeName", TEST_EXCHANGE);
        ReflectionTestUtils.setField(orderEventProducer, "routingKey", TEST_ROUTING_KEY);
    }

    @Test
    @DisplayName("Should publish OrderEvent to RabbitMQ with correct exchange and routing key")
    void publishOrderEvent_shouldSendMessageToCorrectExchangeAndRoutingKey() {
        // Arrange
        OrderEvent event = new OrderEvent(1001L, "CUST-001", LocalDateTime.now());

        // Act
        orderEventProducer.publishOrderEvent(event);

        // Assert - verify RabbitTemplate was called with the exact exchange and routing key
        verify(rabbitTemplate, times(1))
                .convertAndSend(TEST_EXCHANGE, TEST_ROUTING_KEY, event);
    }

    @Test
    @DisplayName("Should publish the exact OrderEvent payload without modification")
    void publishOrderEvent_shouldPublishCorrectPayload() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        OrderEvent event = new OrderEvent(2002L, "CUST-099", now);

        // Capture the actual argument passed to convertAndSend
        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);

        // Act
        orderEventProducer.publishOrderEvent(event);

        // Assert payload fields are correct
        verify(rabbitTemplate).convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), captor.capture());
        OrderEvent captured = captor.getValue();

        assertEquals(2002L, captured.getOrderId());
        assertEquals("CUST-099", captured.getCustomerId());
        assertEquals(now, captured.getTimestamp());
    }

    @Test
    @DisplayName("Should call convertAndSend exactly once per publishOrderEvent call")
    void publishOrderEvent_shouldBeCalledExactlyOnce() {
        // Arrange
        OrderEvent event = new OrderEvent(3003L, "CUST-010", LocalDateTime.now());

        // Act
        orderEventProducer.publishOrderEvent(event);

        // Assert - no more, no fewer interactions
        verify(rabbitTemplate, times(1))
                .convertAndSend(anyString(), anyString(), any(OrderEvent.class));
        verifyNoMoreInteractions(rabbitTemplate);
    }
}
