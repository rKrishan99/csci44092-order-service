# Order Service

Handles order creation for the CSCI 44092 e-commerce microservices project. Fetches product details from the Product Service, calculates the order total, persists the order, and publishes an order event to RabbitMQ for the Notification Service to consume.

Part of a three-service system: [Product Service](https://github.com/rKrishan99/csci44092-product-service) · **Order Service** (this repo) · [Notification Service](https://github.com/rKrishan99/csci44092-notification-service).

## Stack

- Java 17, Spring Boot 3.5.15
- Spring Data JPA + Hibernate
- Spring AMQP (RabbitMQ)
- PostgreSQL (runtime), H2 (tests)
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito + spring-rabbit-test

## Workflow

1. Client calls `POST /orders` with `customerId`, `productId`, `quantity`.
2. Order Service calls Product Service (`GET /products/{id}`) to fetch `name` and `unitPrice`.
3. Computes `totalPrice = quantity × unitPrice` and saves the order.
4. Publishes an `OrderEvent` to the `order.exchange` → `order.queue`.
5. Notification Service consumes the event and logs a mock notification.

## Running locally

Requires PostgreSQL and RabbitMQ reachable at `localhost` — both are provided by the shared `docker-compose.yml` at the root of the three-repo workspace (creates the `order_db` database automatically):

```bash
docker compose up -d postgres rabbitmq
```

Start the **Product Service** first (Order Service calls it synchronously when creating an order), then this service:

```bash
./mvnw spring-boot:run
```

The app starts on **port 8082**.

## Configuration

Environment-variable driven — nothing hardcoded (see `src/main/resources/application.properties`):

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `RABBITMQ_PORT` | `5672` | RabbitMQ port |
| `RABBITMQ_USER` | `guest` | RabbitMQ user |
| `RABBITMQ_PASS` | `guest` | RabbitMQ password |
| `PRODUCT_SERVICE_URL` | `http://localhost:8081` | Base URL for the Product Service |

## API

Interactive docs: `http://localhost:8082/swagger-ui/index.html`

| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create an order (fetches product, prices it, saves, publishes event) |
| `GET` | `/orders/{id}` | Get an order by ID |

**Order entity:** `orderId`, `customerId`, `productId`, `productName`, `quantity`, `totalPrice`, `orderDate`.

**Example — create an order:**
```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-100","productId":1,"quantity":2}'
```

## Messaging

- Exchange: `order.exchange`
- Queue: `order.queue`
- Routing key: `order.created`

Check delivery via the RabbitMQ management UI at `http://localhost:15672` (`guest` / `guest`).

## Tests

```bash
./mvnw test
```

Covers the controller, service, and RabbitMQ producer layers (`OrderControllerTest`, `OrderServiceTest`, `OrderEventProducerTest`).

## API testing (Postman)

A ready-made collection covering Create/Get Product, Delete Product, and Create/Get Order is at [`tests/Product_Order_APIs.postman_collection.json`](tests/Product_Order_APIs.postman_collection.json).

## CI/CD

`.github/workflows/ci.yml` runs on every pull request to `main`: builds with Maven, runs the unit test suite, then runs SonarCloud static analysis (requires a `SONAR_TOKEN` repository secret).
