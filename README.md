# TMAG Spring Server

`spring-server` is the backend API for Travel Medicine Advisory Global. It handles authentication, authorization, users, companies, travel plans, family plans, doctor validation, affiliate tracking, credits, invoices, ebooks, payments, email, storage, AI plan generation, PDFs, reporting, and administration APIs.

## Stack

- Spring Boot 3.5
- Java 25
- Maven Wrapper
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Redis cache support
- Flyway migrations
- Spring Mail
- WebSocket support
- Springdoc OpenAPI
- Google Cloud Storage and S3-compatible storage clients
- Flutterwave and Paystack integrations
- Vertex, OpenAI, and Anthropic AI provider configuration

## Local URL

The server defaults to port `8080`:

```text
http://localhost:8080
```

API routes are primarily under:

```text
/api/v1
```

OpenAPI docs:

```text
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui
http://localhost:8080/swagger-ui.html
```

## Setup

```bash
cd spring-server
cp .env.example .env
```

Create a local PostgreSQL database, then update `.env` with local values and secrets. Do not commit `.env`.

Start the server:

```bash
./mvnw spring-boot:run
```

## Commands

| Command | Description |
| --- | --- |
| `./mvnw spring-boot:run` | Start the API on port `8080` unless `SERVER_PORT` overrides it. |
| `./mvnw compile` | Compile Java sources. |
| `./mvnw test` | Run tests. |
| `./mvnw test -Dtest=ClassName` | Run one test class. |
| `./mvnw clean package` | Build the application JAR. |

## Environment variables

Use `spring-server/.env` for local configuration. The file is loaded by `spring-dotenv`.

### Application and links

| Variable | Purpose |
| --- | --- |
| `APP_ENV` | Runtime environment label such as `debug`, `development`, or `production`. |
| `SERVER_PORT` | HTTP port; defaults to `8080`. |
| `APP_HOST` | Public backend base URL used in generated docs and callbacks. |
| `APP_FRONTEND_URL` | Public client app URL. |
| `APP_SUPER_ADMIN_APP` | Super Admin app URL. |
| `APP_ADMIN_APP` | Company Admin Dashboard URL. |
| `APP_AFFILIATE_APP` | Affiliate Dashboard URL. |
| `APP_SUPPORT_APP` | Support Console URL. Set to `http://localhost:3003` when using this repo's support app locally. |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated browser origins allowed to call the API. |
| `APP_ADMIN_EMAIL` | Public support/admin contact email. |

### Database and migrations

| Variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL. |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username. |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password. |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate schema mode. Use non-destructive values outside disposable local databases. |
| `SPRING_FLYWAY_ENABLED` | Enable or disable Flyway migrations. |
| `SPRING_FLYWAY_BASELINE_ON_MIGRATE` | Baseline existing databases before migration. |
| `SPRING_FLYWAY_BASELINE_VERSION` | Flyway baseline version. |

### Security

| Variable | Purpose |
| --- | --- |
| `JWT_SECRET` | Secret used to sign JWTs. Use a long random value. |
| `APP_API_KEY` | API key expected by frontend apps in the `X-Api-Key` header. |

### Email

| Variable | Purpose |
| --- | --- |
| `SPRING_MAIL_HOST` | SMTP host. |
| `SPRING_MAIL_PORT` | SMTP port. |
| `SPRING_MAIL_USERNAME` | SMTP username. |
| `SPRING_MAIL_PASSWORD` | SMTP password or provider token. |
| `SPRING_MAIL_SMTP_AUTH` | Enable SMTP authentication. |
| `SPRING_MAIL_SMTP_STARTTLS_ENABLE` | Enable STARTTLS. |
| `SPRING_MAIL_TEST_CONNECTION` | Test SMTP connection at startup. |

### Storage

| Variable | Purpose |
| --- | --- |
| `APP_STORAGE_PROVIDER` | Storage provider: `local`, `gcs`, `s3`, or `r2`. |
| `APP_STORAGE_PATH` | Local upload path. |
| `APP_STORAGE_BASE_URL` | Public base URL for uploaded files. |
| `APP_STORAGE_MAX_SIZE` | Maximum upload size in bytes. |
| `APP_STORAGE_MAX_FILE_SIZE` | Spring multipart file limit. |
| `APP_STORAGE_MAX_REQUEST_SIZE` | Spring multipart request limit. |
| `APP_STORAGE_GCS_BUCKET` / `APP_STORAGE_GCS_PROJECT_ID` | Google Cloud Storage settings. |
| `APP_STORAGE_R2_ACCOUNT_ID`, `APP_STORAGE_R2_BUCKET`, `APP_STORAGE_R2_ACCESS_KEY`, `APP_STORAGE_R2_SECRET_KEY` | Cloudflare R2 settings used when `APP_STORAGE_PROVIDER=r2`. The R2 token must have Object Read & Write permission for the bucket. |
| `APP_STORAGE_R2_PUBLIC_URL` | Optional public base URL for R2 objects, such as `https://pub-xxxx.r2.dev` or a custom domain. |
| `APP_STORAGE_S3_BUCKET`, `APP_STORAGE_S3_REGION`, `APP_STORAGE_S3_ACCESS_KEY`, `APP_STORAGE_S3_SECRET_KEY`, `APP_STORAGE_S3_ENDPOINT` | AWS S3 or MinIO-compatible storage settings used when `APP_STORAGE_PROVIDER=s3`. |

### Cache and queues

| Variable | Purpose |
| --- | --- |
| `REDIS_HOST` | Redis host. |
| `REDIS_PORT` | Redis port. |
| `REDIS_PASSWORD` | Redis password when required. |
| `APP_WEBSOCKET_ALLOWED_ORIGINS` | Allowed origins for WebSocket clients. |

### AI generation

| Variable | Purpose |
| --- | --- |
| `APP_AI_PROVIDER` | Default provider, such as `vertex`, `openai`, or `anthropic`. |
| `APP_AI_MAIN_PROVIDER`, `APP_AI_SUMMARY_PROVIDER`, `APP_AI_FREE_PROVIDER`, `APP_AI_STANDARD_PREMIUM_PROVIDER` | Provider selection per plan generation flow. |
| `APP_AI_DEFAULT_MODEL`, `APP_AI_MAIN_MODEL`, `APP_AI_SUMMARY_MODEL`, `APP_AI_FREE_MODEL`, `APP_AI_STANDARD_PREMIUM_MODEL` | Model selection per flow. |
| `APP_AI_TEMPERATURE` | Generation temperature. |
| `APP_AI_MAX_OUTPUT_TOKENS`, `APP_AI_SUMMARY_MAX_OUTPUT_TOKENS` | Output limits. |
| `VERTEX_PROJECT_ID`, `VERTEX_LOCATION`, `VERTEX_MODEL`, `VERTEX_CREDENTIALS_PATH` | Vertex AI settings. |
| `OPENAI_API_KEY`, `OPENAI_MODEL`, `OPENAI_BASE_URL` | OpenAI-compatible provider settings. |
| `ANTHROPIC_API_KEY`, `ANTHROPIC_MODEL`, `ANTHROPIC_BASE_URL` | Anthropic provider settings. |

### Payments

| Variable | Purpose |
| --- | --- |
| `FLUTTERWAVE_PUBLIC_KEY` | Flutterwave public key. |
| `FLUTTERWAVE_SECRET_KEY` | Flutterwave secret key. |
| `FLUTTERWAVE_ENCRYPTION_KEY` | Flutterwave encryption key. |
| `FLUTTERWAVE_WEBHOOK_URL` | Flutterwave webhook endpoint. |
| `FLUTTERWAVE_WEBHOOK_SECRET_HASH` | Optional webhook signature/hash secret. |
| `PAYSTACK_SECRET_KEY` | Paystack secret key for bank and payout workflows. |

### Logging

| Variable | Purpose |
| --- | --- |
| `APP_LOGGING_ERROR_FILE_ENABLED` | Enable error file logging. |
| `APP_LOGGING_ERROR_FILE_PATH` | Error log file path. |
| `APP_LOGGING_ERROR_FILE_MAX_SIZE` | Error log rotation size. |
| `APP_LOGGING_ERROR_FILE_MAX_HISTORY` | Error log retention count. |

## Project structure

```text
spring-server/
├── src/main/java/com/TravelMedicineAdvisory/Server/
│   ├── config/       # Security, OpenAPI, callbacks, and application configuration
│   ├── core/         # Cross-cutting services such as email, payment, storage, cache, currency, and seeders
│   ├── domain/       # Domain packages with controllers, services, repositories, entities, DTOs
│   ├── middlewares/  # Web MVC and request middleware configuration
│   ├── security/     # JWT, filters, user details, and auth support
│   └── ServerApplication.java
├── src/main/resources/
│   ├── db/migration/ # Flyway SQL migrations
│   ├── fonts/        # PDF font assets
│   ├── static/       # Static resources and prompts
│   ├── templates/    # Template resources
│   └── application.properties
├── src/test/         # Test sources
├── .env.example      # Local environment template without real secrets
├── mvnw / mvnw.cmd   # Maven Wrapper
└── pom.xml
```

## API conventions

- Most API endpoints are under `/api/v1`.
- Most protected routes require both `Authorization: Bearer <jwt>` and `X-Api-Key` when API-key middleware is enabled.
- Public or callback routes include auth, selected catalog/read endpoints, payment webhooks/callbacks, Swagger/OpenAPI, storage, and configured public resources.
- Platform admin routes are under `/api/v1/admin/*`.
- Company admin routes are under `/api/v1/company-admin/*`.
- Affiliate routes are under `/api/v1/affiliate/*` and `/api/v1/public/affiliate/*`.

## Development workflow

1. Start PostgreSQL and Redis.
2. Fill `spring-server/.env` with local values.
3. Run `./mvnw spring-boot:run`.
4. Open Swagger UI and verify the API key/JWT auth flow.
5. Run `./mvnw compile` and `./mvnw test` before handing off backend changes.

## Deployment notes

- Replace all local placeholder secrets with managed secrets in the deployment environment.
- Use a non-destructive `SPRING_JPA_HIBERNATE_DDL_AUTO` value outside throwaway local databases.
- Review Flyway support and `SPRING_FLYWAY_ENABLED` before running migrations in shared environments.
- Set CORS origins to exact deployed frontend origins.
- Disable Swagger in production when it should not be publicly visible.
