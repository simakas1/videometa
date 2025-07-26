# Video Meta API

A Spring Boot REST API for managing video metadata, secured with JWT-based authentication.

## Tech Stack

- **Java 21** — Programming language
- **Spring Boot** — Application framework
- **Maven** — Project build and dependency manager
- **JUnit** — For unit testing
- **PostgreSQL** — Relational database for storing metadata
- **Redis** — In-memory cache for performance
- **RabbitMQ** — Message broker used for asynchronous video imports
- **Docker** — Containerization for consistent dev and deployment
- **Mockoon** — Simulates external video metadata services

## Project Structure

```
src/main
 ├── java/lt/svaskevicius/videometa
 │    ├── config        # App configuration
 │    ├── dal           # Data access layer (repositories, entities)
 │    ├── exception     # Custom exception handling
 │    ├── integration   # Interfaces to external systems
 │    ├── mapper        # Object mapping logic
 │    ├── security      # JWT and user authentication
 │    ├── service       # Business logic
 │    └── web           # REST controllers and related services
 └── resources/http     # HTTP scripts for testing API endpoints
```

## Design decisions

- **Monolithic Architecture**: The application is currently built as a monolith to reduce complexity during development
  and deployment. In the future, the video import component could be moved to a separate service, allowing it to scale
  independently from the main API.
- **Asynchronous Video Import**: Video import operations are processed in the background using RabbitMQ. This keeps the
  main API responsive by offloading time-consuming tasks to a message queue.
- **Caching with Redis**: Redis is used as a distributed cache to support a multi-node setup. Unlike in-memory caches
  that are local to a single instance, Redis allows all instances to access the same cached data, ensuring consistency
  across the system.
- **Database**: PostgreSQL was chosen for reliability and strong support for relational data. In the future, if
  analytics workloads grows, a
  read-optimized or columnar database could be added to improve performance on
  big queries.
- **RabbitMQ**: RabbitMQ is used because it's easy to set up and suitable for the current scale of the project. For
  cloud environments, a managed solution like AWS SQS could be more appropriate. For higher throughput or more complex
  messaging patterns, Kafka might be a better fit.
- **Statistics**: Using a database view for statistics lets the database efficiently handle calculations, improving
  performance. It simplifies backend code and ensures data is always fresh and consistent.

## Suggested Future Improvements

- Modularize the project for better maintainability
- Add refresh token support for JWT authentication or migrate to Spring Authorization server
- Add integration tests
- Implement rate limiting for endpoint protection
- Improve video import by saving each video separately on a separate queue
- Add retry mechanisms for failed imports

## Authentication & Authorization

All endpoints except `POST /auth/login` require JWT authentication.

**Roles (Authorities):**

- `USER` — Can read video data
- `ADMIN` — Full access to all API features
- `VIDEO_IMPORTER` — Can import new videos
- `VIDEO_ANALYTICS` — Can view analytics

> Users can have multiple roles (e.g., both `USER` and `VIDEO_IMPORTER`).

### Predefined Users

| Username                          | Password | Roles                                 |
|-----------------------------------|----------|---------------------------------------|
| `admin`                           | changeme | All roles                             |
| `default`                         | changeme | USER                                  |
| `analytic`                        | changeme | USER, VIDEO_ANALYTICS                 |
| `default_video_importer_analytic` | changeme | USER, VIDEO_IMPORTER, VIDEO_ANALYTICS |

## Getting Started

### Step 1: Clone the Project

```bash
git clone git@github.com:simakas1/videometa.git
cd videometa
```

### Step 2: Run the Project

**Option A: With Docker**

```bash
docker build -t videometa:latest .
docker compose up
```

**Option B: With Maven**

1. Start the required services (PostgreSQL, Redis, Mockoon) using Docker Compose:

```bash
docker compose up
```

2. Build and run the project:

```bash
mvn clean install
mvn spring-boot:run
```

### Step 3: Authenticate

Use the `/auth/login` endpoint to get a JWT token.

**Example:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login 
-H "Content-Type: application/json"   
-d '{
    "username": "your_username",
    "password": "your_password"
  }'
```

Use this token in the `Authorization` header for all other requests:

```http
Authorization: Bearer <your-jwt-token>
```

> ### Suggestion
> You can also find ready-to-use HTTP request scripts in the `resources/http` directory.

### Step 4: View API Docs

Swagger UI is available at:

```
http://localhost:8080/api/v1/swagger-ui/index.html
```
