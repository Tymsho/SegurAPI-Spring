# 🛡️ SegurAPI — Insurance Policy & Portfolio Management System

RESTful API built with **Java 17** and **Spring Boot 3.5**, designed for the comprehensive management of client portfolios, insurance policies, insurance companies, data imports, and notifications for Insurance Brokers and Producers.

---

## ⚙️ Architecture & Core Features

* **Layered Architecture:** Strict separation of concerns across Controllers (`REST`), Services (business logic), Repositories (`Spring Data JPA`), and data transfer via `DTOs` (Requests/Responses).
* **Stateless Security (JWT):** JSON Web Token-based authentication, password hashing with `BCrypt`, dynamic CORS configuration, and role-based access control (`ADMIN` and `PRODUCTOR`).
* **Cloud-Ready Database (PostgreSQL):** Configured for **PostgreSQL / Supabase** with a **HikariCP** connection pool optimized for cloud and serverless environments.
* **System Modules:**
  * **Client** and **Policy** management featuring advanced filtering and custom queries (`JPQL`).
  * Pre-configured catalog of **Insurance Companies**, **Insurance Lines (Ramos)**, and **Geography** (Provinces and Cities).
  * **Import Template** management and automated email **Notification** system (`SMTP`).
* **Centralized Exception Handling:** Declarative validations (`@Valid`) and global error interception via `GlobalExceptionHandler` returning standardized HTTP responses.
* **Automated Testing:** Unit and integration testing across service and controller layers using `JUnit 5`, `Mockito`, and `MockMvc`.

---

## 🛠️ Tech Stack

* **Language & Framework:** Java 17, Spring Boot 3.5.x
* **Persistence:** Spring Data JPA, Hibernate, PostgreSQL Driver, HikariCP
* **Security:** Spring Security, JJWT (JSON Web Token)
* **Notifications:** Spring Boot Starter Mail (SMTP)
* **Testing:** JUnit 5, Mockito, MockMvc

---

## 🚀 Configuration & Environment Variables

The project natively supports loading `.env` files located in the project root or environment variables injected directly by the deployment host.

Create a `.env` file in the root directory based on the following template:

```properties
# Server
PORT=8080

# Database (PostgreSQL / Supabase Session Pooler)
DB_URL=jdbc:postgresql://[aws-0-sa-east-1.pooler.supabase.com:5432/postgres?prepareThreshold=0&sslmode=require](https://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?prepareThreshold=0&sslmode=require)
DB_USERNAME=postgres.your_project_ref
DB_PASSWORD=your_secure_password
DB_POOL_SIZE=5

# Hibernate / JPA
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

# JWT Security (Minimum 32 bytes / 256 bits)
JWT_SECRET=YourProductionSecretKeyWithMoreThan32BytesLength
JWT_EXPIRATION=86400000

# CORS (Comma-separated allowed origins)
CORS_ALLOWED_ORIGINS=http://localhost:5173,[https://your-frontend.vercel.app](https://your-frontend.vercel.app)

# Seeder & Initial Admin User
SEEDER_ENABLED=true
ADMIN_EMAIL=your_email@domain.com
ADMIN_NOMBRE=Administrator
ADMIN_PASSWORD=YourSecureAdminPassword

# Mail Server (Gmail SMTP)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

---

## 💻 Local Execution

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/your-username/SegurAPI-Spring.git](https://github.com/your-username/SegurAPI-Spring.git)
   cd SegurAPI-Spring
   ```
2. **Configure the environment:**
   Create the `.env` file using the credentials from your local PostgreSQL instance or your Supabase project.
3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   The API will be available at `http://localhost:8080`.

---

## 🌱 Data Initialization (`DataSeeder`)

When starting the application with `SEEDER_ENABLED=true`, the `DataSeeder.java` component idempotently verifies and initializes only the base catalogs:
1. **Initial Geography:** Base provinces and cities (if the table is empty).
2. **Official Catalog:** 37 insurance companies and 10 primary insurance lines.
3. **Administrator User:** If no user exists matching the email defined in `ADMIN_EMAIL`, it automatically creates one with the `ADMIN` role and hashes the password defined in `ADMIN_PASSWORD` using `BCrypt`. No mock clients, test policies, or default hardcoded credentials are injected.

---

## 🔑 Authentication & API Usage

To consume protected endpoints, send a `POST` request to `/api/auth/login` with the credentials configured in your environment variables:

```json
{
  "email": "your_email@domain.com",
  "password": "YourSecureAdminPassword"
}
```

The response will return a JWT token, which must be included in the header of subsequent requests:
```http
Authorization: Bearer <your_jwt_token>
```
