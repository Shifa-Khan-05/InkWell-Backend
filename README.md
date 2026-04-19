# 🛡️ InkWell Auth & User Service

The **Auth & User Service** is the core identity and security module of the **InkWell Blogging Platform**. It handles authentication, authorization, and user profile management while ensuring secure access through modern standards like JWT and OAuth2.

---

## 📌 Overview

This service is responsible for:

* Managing user accounts and profiles
* Handling secure authentication using JWT
* Enforcing Role-Based Access Control (RBAC)
* Supporting social login via OAuth2 providers
* Managing account lifecycle (activation, deactivation)

It acts as the **central security layer** for all other microservices in the InkWell ecosystem.

---

## 🚀 Key Features

### 👤 User Management

* Create, update, retrieve, and manage user profiles
* Supports user bio and avatar customization
* Soft account deactivation capability

### 🔐 Security & Authentication

* Password encryption using **BCrypt**
* Stateless authentication via **JWT (JSON Web Tokens)**
* Secure login and token validation
* CORS configuration for frontend integration

### 🧩 Role-Based Access Control (RBAC)

| Role       | Permissions                          |
| ---------- | ------------------------------------ |
| **READER** | Browse posts, comment                |
| **AUTHOR** | Create, edit, and manage own content |
| **ADMIN**  | Full system control, user management |

### 🌐 OAuth2 Integration

* Google login support
* GitHub login support
* Automatic account creation for social users

### 🔄 Account Lifecycle Management

* Activate / deactivate users
* Suspend accounts for policy violations

---

## 🛠️ Tech Stack

| Layer     | Technology            |
| --------- | --------------------- |
| Language  | Java 17               |
| Framework | Spring Boot 3.4.2     |
| Security  | Spring Security + JWT |
| Database  | MySQL                 |
| ORM       | Spring Data JPA       |
| Mapping   | ModelMapper           |
| Utilities | Lombok                |

---

## 🏗️ Architecture

This service strictly follows the **BridgeLabz Case Study Class Diagram**.

### 📦 Core Components

#### 🧾 User Entity

Stores:

* `userId`
* `username`
* `email`
* `passwordHash`
* `role`
* `bio`
* `avatarUrl`
* `provider` (OAuth source)
* `isActive`

#### ⚙️ AuthService

Handles business logic:

* `register()`
* `login()`
* `validateToken()`
* `deactivateAccount()`

#### 🌐 AuthResource (Controller)

Exposes REST APIs for frontend communication.

---

## 📡 API Endpoints

| Method   | Endpoint                 | Access        | Description              |
| -------- | ------------------------ | ------------- | ------------------------ |
| **POST** | `/auth/register`         | Public        | Register a new user      |
| **POST** | `/auth/login`            | Public        | Authenticate and get JWT |
| **GET**  | `/auth/profile/{id}`     | Authenticated | Get user profile         |
| **PUT**  | `/auth/profile/{id}`     | Authenticated | Update profile           |
| **POST** | `/auth/logout`           | Authenticated | Logout user              |
| **PUT**  | `/admin/deactivate/{id}` | Admin         | Deactivate user          |

---

## ⚙️ Setup & Installation

### 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/inkwell-auth-service.git
cd inkwell-auth-service
```

---

### 2️⃣ Database Setup

Create MySQL database:

```sql
CREATE DATABASE inkwell_auth;
```

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inkwell_auth
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

### 3️⃣ Environment Variables (Recommended)

```env
JWT_SECRET=your_secret_key
JWT_EXPIRATION=86400000
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_secret
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_secret
```

---

### 4️⃣ Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

Application will start on:

```
http://localhost:8081
```

---

## 🔐 Authentication Flow

1. User registers or logs in
2. Server validates credentials
3. JWT token is generated
4. Client sends token in headers
5. Backend validates token for each request

---

## 🧪 Testing

You can test APIs using:

* Postman
* Swagger 
* Curl

Example:

```bash
curl -X POST http://localhost:8081/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"test@example.com","password":"123456"}'
```

---

## 📁 Project Structure

```
src/main/java/com/inkwell/auth
│
├── config          # Security & JWT configs
├── controller      # REST controllers
├── service         # Business logic
├── repository      # JPA repositories
├── entity          # Database models
├── dto             # Request/Response DTOs
└── util            # Helper classes
```

---

## 🔒 Security Best Practices Implemented

* Password hashing with BCrypt
* Stateless authentication
* Role-based authorization
* Secure token validation
* OAuth2 login support

---

## 🚧 Future Enhancements

* Refresh Token implementation
* Email verification system
* Multi-factor authentication (MFA)
* Rate limiting & brute-force protection
* Audit logging

---
