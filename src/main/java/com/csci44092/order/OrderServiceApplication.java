package com.csci44092.order;

import com.csci44092.order.messaging.OrderEvent;
import com.csci44092.order.messaging.OrderEventProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

// NOTE for Member 3 (CS-2020-029): Remove the 'exclude' list below once you
// configure PostgreSQL in application.properties and add the Order JPA entity.
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

	/**
	 * Demo runner: publishes a sample OrderEvent on startup to verify the
	 * RabbitMQ producer is wired correctly.
	 *
	 * NOTE: This is a placeholder integration point. Member 3 (CS-2020-029)
	 * will call orderEventProducer.publishOrderEvent(...) from within the
	 * Order Service layer after a real order is saved to the database.
	 */
	@Bean
	public CommandLineRunner demoOrderEventPublisher(OrderEventProducer orderEventProducer) {
		return args -> {
			OrderEvent demoEvent = new OrderEvent(1001L, "CUST-001", LocalDateTime.now());
			orderEventProducer.publishOrderEvent(demoEvent);
		};
	}
}
