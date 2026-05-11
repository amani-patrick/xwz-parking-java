# XWZ Parking Management System — Java Spring Boot Microservices

> **Package:** `com.amnii.parking.*`  
> **Stack:** Java 21 · Spring Boot 3.2 · Spring Cloud 2023 · PostgreSQL · RabbitMQ · Eureka

---

## Architecture

```
                     ┌─────────────────────────────────┐
                     │   CLIENT (Frontend / Postman)   │
                     └────────────────┬────────────────┘
                                      │ HTTP
                     ┌────────────────▼────────────────┐
                     │    API GATEWAY  :8080            │
                     │  JWT Validation · CORS · Routes │
                     └──┬──────┬────────┬──────┬───────┘
                        │      │        │      │
              ┌─────────▼─┐ ┌──▼──┐ ┌──▼──┐ ┌▼────────┐
              │   AUTH    │ │PARK │ │ENTRY│ │REPORT   │
              │  :8081    │ │:8082│ │:8083│ │ :8084   │
              └─────┬─────┘ └──┬──┘ └──┬──┘ └────┬────┘
                    │          │        │          │
                    │    ┌─────▼────────▼──┐       │
                    │    │   PostgreSQL    │◄──────┘
                    │    │    :5432        │
                    │    └─────────────────┘
                    │
                    │  RabbitMQ Events
          ┌─────────▼──────────────────────────────────┐
          │              RABBITMQ  :5672               │
          │                                            │
          │  xwz.user.exchange   ──► user.registered  │
          │  xwz.entry.exchange  ──► car.entered       │
          │                      ──► car.exited        │
          └─────────┬──────────────────────────────────┘
                    │
          ┌─────────▼─────────┐
          │  NOTIFICATION     │
          │  SERVICE  :8085   │
          │  Listens all evts │
          └───────────────────┘

  Service Discovery: Eureka  :8761
  RabbitMQ Management UI:    :15672
```

---

## Services

| Service              | Port | Package                              | Description |
|----------------------|------|--------------------------------------|-------------|
| **Discovery Server** | 8761 | `com.amnii.parking.discovery`        | Eureka service registry |
| **API Gateway**      | 8080 | `com.amnii.parking.gateway`          | JWT filter, routing, CORS |
| **Auth Service**     | 8081 | `com.amnii.parking.auth`             | Registration, login, JWT, users |
| **Parking Service**  | 8082 | `com.amnii.parking.parkingservice`   | Parking CRUD + space tracking |
| **Entry Service**    | 8083 | `com.amnii.parking.entryservice`     | Car entry/exit, ticket, bill |
| **Report Service**   | 8084 | `com.amnii.parking.reportservice`    | Analytics, date-range reports |
| **Notification Svc** | 8085 | `com.amnii.parking.notification`     | RabbitMQ consumer, email alerts |

---

## RabbitMQ Message Flow

```
AUTH-SERVICE  ──publishes──►  xwz.user.exchange  (user.registered)
                                     │
                              ┌──────▼──────────────────────────────┐
                              │  xwz.user.registered (main queue)   │
                              │  xwz.notif.user.registered (notif)  │
                              └──────────────────────────────────────┘
                                     │
                           NOTIFICATION-SERVICE ◄── listens ──┘

ENTRY-SERVICE ──publishes──►  xwz.entry.exchange
                              ├── car.entered ──► xwz.car.entered
                              │                   xwz.notif.car.entered
                              └── car.exited  ──► xwz.car.exited
                                                  xwz.notif.car.exited
```

---

## Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 15+
- RabbitMQ 3.12+
- Docker & Docker Compose (for containerised run)

---

## Quick Start

### Option A — Docker Compose (recommended)

```bash
cd xwz-java
docker-compose up --build
```

Services start in order: Postgres → RabbitMQ → Eureka → Gateway → Auth (runs Flyway) → others.

### Option B — Manual

#### 1. Database

```sql
-- in psql
CREATE USER xwz WITH PASSWORD 'xwz_secret';
CREATE DATABASE xwz_parking OWNER xwz;
GRANT ALL PRIVILEGES ON DATABASE xwz_parking TO xwz;
```

#### 2. RabbitMQ

```bash
# Using Docker
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3.12-management-alpine
```

#### 3. Build all

```bash
cd xwz-java
mvn clean install -DskipTests
```

#### 4. Start in order

```bash
# Terminal 1
cd discovery-server && mvn spring-boot:run

# Terminal 2 (wait for Eureka)
cd api-gateway && mvn spring-boot:run

# Terminal 3 (runs Flyway migrations)
cd auth-service && mvn spring-boot:run

# Terminals 4-7
cd parking-service      && mvn spring-boot:run
cd entry-service        && mvn spring-boot:run
cd report-service       && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

---

## Default Credentials

| Role  | Email                    | Password    |
|-------|--------------------------|-------------|
| Admin | `admin@xwzparking.rw`   | `Admin@1234` |

---

## API Documentation (Swagger UI)

| Service         | Swagger URL |
|-----------------|-------------|
| Auth Service    | http://localhost:8081/swagger-ui.html |
| Parking Service | http://localhost:8082/swagger-ui.html |
| Entry Service   | http://localhost:8083/swagger-ui.html |
| Report Service  | http://localhost:8084/swagger-ui.html |

All requests flow through the gateway at **http://localhost:8080**.

---

## API Endpoints (via Gateway)

### Auth — `/api/auth`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, get JWT |
| GET  | `/api/auth/me` | Any | Get own profile |
| GET  | `/api/auth/users` | Admin | List all users (paginated) |
| PATCH | `/api/auth/users/{id}/toggle` | Admin | Activate/deactivate user |

### Parkings — `/api/parkings`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/parkings` | Admin | Register parking |
| GET  | `/api/parkings` | Any | List parkings (paginated) |
| GET  | `/api/parkings/{code}` | Any | Get parking by code |
| PUT  | `/api/parkings/{code}` | Admin | Update parking |
| DELETE | `/api/parkings/{code}` | Admin | Deactivate parking |

### Entries — `/api/entries`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/entries` | Any | Register car entry → ticket |
| PATCH | `/api/entries/{id}/exit` | Any | Register exit → bill |
| GET  | `/api/entries` | Any | List entries (paginated) |
| GET  | `/api/entries/{id}` | Any | Get single entry |

### Reports — `/api/reports`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/reports/dashboard` | Admin | Real-time dashboard stats |
| GET | `/api/reports/outgoing?startDate=&endDate=` | Admin | Outgoing + revenue report |
| GET | `/api/reports/entered?startDate=&endDate=` | Admin | Entered cars report |
| GET | `/api/reports/parking/{code}/cars` | Any | Cars at specific parking |

**Date format for reports:** `2024-01-15T00:00:00`

---

## Validation Rules

- **Password:** min 8 chars, must have uppercase + lowercase + number + special char (`@$!%*?&`)
- **Exit time:** must be AFTER entry time (prevents "leaving earlier than they entered")
- **Plate number:** uppercase alphanumeric, max 20 chars
- **Parking code:** uppercase alphanumeric, 2–20 chars
- **Fee per hour:** must be ≥ 0
- **Total spaces:** must be ≥ 1
- **Duplicate entry:** same plate in same parking with status PARKED → rejected

---

## Security

- JWT validated at **API Gateway** level (filter applied before routing)
- Role (`ADMIN` / `PARKING_TENANT`) propagated as `X-User-Role` header to services
- Services trust gateway headers — no re-validation needed downstream
- BCrypt password hashing (strength 12)
- CORS configured on gateway
- Flyway manages DB migrations (only auth-service runs them; others use placeholder V1)

---

## Project Structure

```
xwz-java/
├── pom.xml                          ← Parent POM (com.amnii.parking)
├── docker-compose.yml
├── discovery-server/
│   └── src/main/java/com/amnii/parking/discovery/
├── api-gateway/
│   └── src/main/java/com/amnii/parking/gateway/
│       ├── filter/JwtAuthFilter.java
│       └── ApiGatewayApplication.java
├── auth-service/
│   └── src/main/java/com/amnii/parking/auth/
│       ├── entity/User.java
│       ├── dto/AuthDtos.java
│       ├── repository/UserRepository.java
│       ├── service/AuthService.java
│       ├── controller/AuthController.java
│       ├── config/{JwtUtil,RabbitMQConfig,SecurityConfig}.java
│       ├── messaging/UserRegisteredEvent.java
│       └── exception/
├── parking-service/
│   └── src/main/java/com/amnii/parking/parkingservice/
├── entry-service/
│   └── src/main/java/com/amnii/parking/entryservice/
│       ├── messaging/EntryEvents.java       ← CAR_ENTERED / CAR_EXITED
│       ├── config/RabbitMQConfig.java
│       └── service/EntryService.java        ← exit time validation
├── report-service/
│   └── src/main/java/com/amnii/parking/reportservice/
└── notification-service/
    └── src/main/java/com/amnii/parking/notification/
        ├── listener/NotificationListener.java  ← all 3 queues
        ├── service/NotificationService.java    ← email/log
        └── config/RabbitMQConfig.java
```
