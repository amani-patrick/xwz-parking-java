# Goal

1. **GitHub Actions**: Add a CI workflow to build the project automatically on pushes to `main`, skipping builds when only documentation files (e.g., `README.md`, `docs/`) are modified.
2. **Notification Service Configuration**: Identify and document how to configure email credentials. Offer an approach for in-app notifications if emails are disabled.
3. **API Gateway**: Complete the `api-gateway` service, which currently has a `pom.xml` but is missing its source code and configuration.

## User Review Required

> [!IMPORTANT]
> - **API Gateway Security**: The `api-gateway` `pom.xml` includes JWT dependencies. Does the gateway need a custom `JwtAuthenticationFilter` to validate tokens before routing requests, or should the gateway just route the requests and let each microservice handle its own authentication? I plan to add basic routing first, but I can add the filter if you prefer.
> - **In-App Notifications**: Currently, when emails are disabled, the `notification-service` logs the email content to the console. If you want a *true* in-app notification system (e.g., users can see notifications in a web dashboard), we would need to add a database (PostgreSQL) to the `notification-service` to save these events, plus REST endpoints to fetch them. Is logging sufficient for the practical, or do you need database-persisted notifications?

## Proposed Changes

### GitHub Actions Workflow
#### [NEW] .github/workflows/build.yml
- Create a workflow triggered on `push` to `main`.
- Add `paths-ignore` for `*.md`, `docs/**`, and `.gitignore`.
- Set up JDK 21.
- Run `mvn clean package -DskipTests` to ensure the project builds correctly without blocking on tests (or run tests if preferred).

---

### Notification Service (Email / In-App)
- **Email Credentials Location**: The credentials can be provided via environment variables. In `notification-service/src/main/resources/application.yml`, the properties are configured to read:
  - `MAIL_USERNAME` (defaults to `noreply@xwzparking.rw`)
  - `MAIL_PASSWORD` (defaults to `changeme`)
  - `NOTIFICATION_MAIL_ENABLED` (defaults to `false`). Set to `true` to actually send emails.
  You can place these in your `.env` file or pass them in `docker-compose.yml`.
- **In-App Notifications**: If emails remain disabled, I can add a `Notification` entity and repository to save messages to a database, and an endpoint `GET /api/notifications/{email}` to retrieve them. *(Waiting on your feedback on whether to build this).*

---

### API Gateway
The `api-gateway` folder only contains the `pom.xml` and `Dockerfile`. We need to generate the source code and configuration.

#### [NEW] api-gateway/src/main/java/com/amnii/parking/gateway/ApiGatewayApplication.java
- Standard Spring Boot Application class with `@EnableDiscoveryClient`.

#### [NEW] api-gateway/src/main/resources/application.yml
- Configure Spring Cloud Gateway routes for the microservices:
  - `auth-service` (e.g., `/api/auth/**`)
  - `parking-service` (e.g., `/api/parkings/**`)
  - `entry-service` (e.g., `/api/entries/**`)
  - `report-service` (e.g., `/api/reports/**`)
- Set up Eureka client configuration to discover the services dynamically.

## Verification Plan
### Automated & Manual Verification
- Commit the `.github/workflows/build.yml` file to test if the GitHub action triggers (or simulate it locally).
- Run `mvn clean install` on the `api-gateway` to ensure it compiles.
- Run `docker-compose up` (or start `discovery-server`, `api-gateway`, and `auth-service` locally) to verify the gateway successfully registers with Eureka and routes traffic to the target services.
