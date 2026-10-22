# ShopSphere

ShopSphere is a Spring Boot REST API for managing product categories and products. It uses Spring Data JPA with Microsoft SQL Server.

## Requirements

- JDK 17 or newer
- Microsoft SQL Server, with a database available to the application

The Maven Wrapper is included, so a separate Maven installation is not required.

## Configuration

The application reads these environment variables:

| Variable | Description |
| --- | --- |
| `DB_URL` | SQL Server JDBC connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |

For local development, create a `.env` file in this directory and set those values. Keep real credentials out of Git. A JDBC URL typically has this form:

```text
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ShopSphereDB;encrypt=true;trustServerCertificate=true
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

Make sure SQL Server is running and the database exists before starting the app. Hibernate is configured with `ddl-auto=update` and updates the schema at startup.

## Run with Docker Compose

Docker Compose starts the backend and its own SQL Server Express container. This database is separate from a SQL Server instance already installed on your computer; existing local database contents are not copied into the container.

1. Copy `.env.docker.example` to `.env.docker` and replace both example passwords with strong local development passwords.
2. From this directory, start the services:

   ```powershell
   Copy-Item .env.docker.example .env.docker
   docker compose --env-file .env.docker up --build
   ```

The API is available at `http://localhost:8080`. The Compose database is available to database tools on `localhost:1433`. Its data is kept in the `sqlserver-data` Docker volume when containers stop. The example passwords are placeholders; do not use them outside local development.

To stop the services while preserving database data:

```powershell
docker compose down
```

The application container connects to the database by the Compose service name `db`, not by `localhost`. The existing `.env` file for running the app directly on the host is not used by this Docker Compose setup.

## Run the application

From this directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8080`. Interactive API documentation is available at `http://localhost:8080/swagger-ui/index.html` and the OpenAPI document at `http://localhost:8080/v3/api-docs`.

## Run tests

```powershell
.\mvnw.cmd test
```

The application-context test connects to SQL Server, so configure the database variables above and make sure the database is reachable before running the full test suite.

## API overview

All request and response bodies use JSON. Product creation and updates require an existing category.

| Method | Endpoint | Description | Success status |
| --- | --- | --- | --- |
| `GET` | `/api/categories` | List categories | `200 OK` |
| `GET` | `/api/categories/{id}` | Get a category | `200 OK` |
| `POST` | `/api/categories` | Create a category | `201 Created` |
| `PUT` | `/api/categories/{id}` | Update a category | `200 OK` |
| `DELETE` | `/api/categories/{id}` | Delete a category | `204 No Content` |
| `GET` | `/api/products` | List products | `200 OK` |
| `GET` | `/api/products/{id}` | Get a product | `200 OK` |
| `POST` | `/api/products` | Create a product | `200 OK` |
| `PUT` | `/api/products/{id}` | Update a product | `200 OK` |
| `DELETE` | `/api/products/{id}` | Delete a product | `204 No Content` |

### Example: create a category

```http
POST /api/categories
Content-Type: application/json

{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

### Example: create a product

Use the category ID returned by the category endpoint:

```http
POST /api/products
Content-Type: application/json

{
  "name": "Wireless keyboard",
  "price": 1200000,
  "description": "Mechanical keyboard",
  "stock": 20,
  "categoryId": 1
}
```

Product names and category names must not be blank. Product prices must be greater than zero, stock must be zero or higher, and `categoryId` must be a positive ID for an existing category. Invalid request bodies return `400 Bad Request`; missing resources return `404 Not Found`.

## Project layout

```text
src/main/java/com/shopsphere/
  controller/   REST endpoints
  dto/          Request and response objects
  entity/       JPA entities
  exception/    API error handling
  repository/   Spring Data repositories
  service/      Application operations
```
