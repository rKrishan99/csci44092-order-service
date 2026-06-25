package com.csci44092.order.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Producer component responsible for publishing OrderEvent messages
 * to RabbitMQ whenever a new order is successfully created.
 *
 * This component is intended to be called from the Order Service layer
 * after persisting an order to the database.
 */
@Component
public class OrderEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public OrderEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes an {@link OrderEvent} to RabbitMQ.
     *
     * @param orderEvent the event containing order details to be published
     */
    public void publishOrderEvent(OrderEvent orderEvent) {
        logger.info("Publishing OrderEvent to exchange '{}' with routing key '{}': {}",
                exchangeName, routingKey, orderEvent);

        rabbitTemplate.convertAndSend(exchangeName, routingKey, orderEvent);

        logger.info("OrderEvent published successfully for orderId: {}", orderEvent.getOrderId());
    }
}
