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

## CI/CD & Artifacts

The project is integrated with **GitHub Actions**. Every push to `main` triggers:
1.  **Maven Build**: Compilation and packaging of all services.
2.  **Docker Publishing**: Docker images are automatically built and pushed to **GitHub Container Registry (GHCR)**.

Images are available at: `ghcr.io/amani-patrick/xwz-<service-name>:latest`

---

## Quick Start

### Option A — Docker Compose (Recommended)

1.  **Clone & Configure**:
    ```bash
    git clone https://github.com/amani-patrick/xwz-parking-java.git
    cd xwz-parking-java
    cp .env.example .env # Update secrets if needed
    ```

2.  **Run with Local Build**:
    ```bash
    docker-compose up --build
    ```

3.  **Run with Pre-built Images (GHCR)**:
    Update `docker-compose.yml` to use `image: ghcr.io/amani-patrick/xwz-<service>:latest` then run:
    ```bash
    docker-compose up
    ```

### Option B — Manual Development

#### 1. Infrastructure
Start PostgreSQL and RabbitMQ (via Docker or local install).

#### 2. Build & Run
```bash
# Build Parent and all modules
mvn clean install -DskipTests

# Start Discovery Server (Eureka)
cd discovery-server && mvn spring-boot:run

# Start other services (in separate terminals)
cd api-gateway && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd parking-service && mvn spring-boot:run
# ... etc
```

---

## Default Credentials

| Role  | Email                    | Password    |
|-------|--------------------------|-------------|
| Admin | `admin@xwzparking.rw`   | `Admin@1234` |

---

## API Endpoints (via Gateway :8080)

All requests must include a `Authorization: Bearer <token>` header except for login/register.

### Auth — `/api/auth`
- `POST /api/auth/login` (Public)
- `POST /api/auth/register` (Public)
- `GET /api/auth/me` (Authenticated)

### Parkings — `/api/parkings`
- `GET /api/parkings` (Any)
- `POST /api/parkings` (Admin)

### Entries — `/api/entries`
- `POST /api/entries` (Any)
- `PATCH /api/entries/{id}/exit` (Any)

---

## Project Structure

```
xwz-java/
├── .github/workflows/build.yml      ← CI/CD Pipeline
├── pom.xml                          ← Parent POM
├── docker-compose.yml               ← Microservice Orchestration
├── api-gateway/
│   └── src/main/java/com/amnii/parking/gateway/
│       ├── config/AuthenticationFilter.java  ← Central JWT Security
│       └── ApiGatewayApplication.java
├── auth-service/
│   └── src/main/java/com/amnii/parking/auth/
├── parking-service/                 ← Added spring-security-starter
├── entry-service/                   ← Car entry/exit logic
├── report-service/                  ← Stats & Analytics
└── notification-service/            ← RabbitMQ & Email alerts
```
