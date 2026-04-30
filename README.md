# 🖋️ InkWell Website Controller (BFF)

The **Website Controller** acts as the **Backend-for-Frontend (BFF)** for the InkWell Blogging Platform. It serves as the **single entry point** for the React frontend and orchestrates communication between multiple backend microservices such as **Auth-Service, Post-Service, Comment-Service, Notification-Service, and Payment-Service**.

Instead of the frontend making multiple direct service calls, the BFF simplifies the architecture by aggregating data, handling cross-service workflows, and providing a clean API surface for the UI.

---

## 📌 Overview

In a microservices architecture, directly connecting the frontend to every service creates:

* Complex frontend logic
* Too many API calls
* Difficult state management
* Increased error handling complexity

The Website Controller solves this by acting as a centralized orchestration layer.

It handles:

* Admin analytics dashboard
* User + post aggregation
* Cross-service business logic
* Graceful failure handling
* Simplified frontend integration

---

## 🚀 Core Functionalities

---

## 📊 1. Admin Analytics Dashboard

This is one of the most important features of the BFF.

It provides a **single dashboard response** containing:

### 👤 Identity Tracking

From **Auth-Service**

* Total users
* Active users
* Premium users
* User role distribution

---

### 📝 Content Insights

From **Post-Service**

* Total posts
* Published posts
* Draft posts
* Most viewed posts

---

### ❤️ Engagement Metrics

From **Comment-Service + Notification-Service**

* Total comments
* Total likes
* Recent activity alerts

---

### 🏆 Top Performing Posts

Logic-based sorting to return:

```text id="bff001"
Top 5 Most Liked Posts
```

Used directly in Admin Dashboard cards.

---

### ⚙️ Platform Health

Tracks service availability:

* Auth-Service
* Post-Service
* Comment-Service
* Notification-Service

System status examples:

```text id="bff002"
OPERATIONAL
DEGRADED
FAILED
```

---

## 🔄 2. Service Orchestration

The BFF acts like a smart coordinator.

### Uses OpenFeign Clients for:

* User details
* Posts
* Comments
* Notifications
* Payment status

---

### Example

Instead of React doing:

```text id="bff003"
GET /users
GET /posts
GET /comments
GET /notifications
```

It only calls:

```text id="bff004"
GET /api/admin/summary
```

and receives everything together.

---

## 🛠️ Tech Stack

| Layer         | Technology             |
| ------------- | ---------------------- |
| Language      | Java 17                |
| Framework     | Spring Boot 3.x        |
| Communication | Spring Cloud OpenFeign |
| Discovery     | Eureka Client          |
| Utilities     | Lombok                 |
| Monitoring    | Spring Boot Actuator   |

---

## 🏗️ System Architecture

```text id="bff005"
React Frontend
      ↓
Website Controller (BFF)
      ↓
────────────────────────────
Auth-Service
Post-Service
Comment-Service
Notification-Service
Payment-Service
────────────────────────────
```

---

## 📡 API Endpoints

---

## 🔹 Admin Endpoints

| Method | Endpoint             | Description                  |
| ------ | -------------------- | ---------------------------- |
| GET    | `/api/admin/summary` | Full admin dashboard summary |
| GET    | `/api/admin/users`   | Fetch all users              |
| GET    | `/api/admin/health`  | Service health check         |

---

## 🔹 Post Endpoints (Proxied)

| Method | Endpoint               | Description        |
| ------ | ---------------------- | ------------------ |
| GET    | `/api/posts/published` | Public feed        |
| POST   | `/api/posts/create`    | Create post        |
| GET    | `/api/posts/{slug}`    | Fetch post details |

---

## 🔹 User Endpoints

| Method | Endpoint            | Description    |
| ------ | ------------------- | -------------- |
| GET    | `/api/profile/{id}` | Get profile    |
| PUT    | `/api/profile/{id}` | Update profile |

---

## 🔹 Notification Endpoints

| Method | Endpoint                        | Description        |
| ------ | ------------------------------- | ------------------ |
| GET    | `/api/notifications/{id}`       | User notifications |
| GET    | `/api/notifications/count/{id}` | Unread count       |

---

## 🔄 Example Workflow (Important 🔥)

## Admin Dashboard Request

### Step 1

Frontend calls:

```text id="bff006"
GET /api/admin/summary
```

---

### Step 2

BFF calls:

* Auth-Service
* Post-Service
* Comment-Service
* Notification-Service

---

### Step 3

BFF merges all data

↓

Creates unified DTO

↓

Returns one clean JSON response

---

### Step 4

Frontend renders dashboard instantly

---

## ⚙️ Configuration

### application.yml

```yaml id="bff007"
server:
  port: 8080

spring:
  application:
    name: website-controller

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

AUTH_SERVICE_URL: http://AUTH-SERVICE
POST_SERVICE_URL: http://POST-SERVICE
COMMENT_SERVICE_URL: http://COMMENT-SERVICE
NOTIFICATION_SERVICE_URL: http://NOTIFICATION-SERVICE
PAYMENT_SERVICE_URL: http://PAYMENT-SERVICE
```

---

## 🚦 Setup & Installation

### ✅ Prerequisites

* Java 17
* Maven
* Eureka Server running on `8761`
* Core services running:

  * Auth-Service
  * Post-Service

Optional:

* Comment-Service
* Notification-Service
* Payment-Service

---

### ▶️ Run Application

```bash id="bff008"
mvn clean install
mvn spring-boot:run
```

---

### 🔍 Verification

Health Check:

```text id="bff009"
http://localhost:8080/actuator/health
```

Swagger (if enabled):

```text id="bff010"
http://localhost:8080/swagger-ui.html
```

---

## 🛡️ Error Handling Strategy

The Website Controller uses:

# Graceful Degradation

### OPERATIONAL

All services working

---

### DEGRADED

Auxiliary services down

Example:

```text id="bff011"
Comment-Service unavailable
```

Dashboard still works

---

### FAILED

Critical services down

Example:

```text id="bff012"
Auth-Service + Post-Service down
```

Returns structured JSON error for frontend Toastify.

---

## 📁 Project Structure

```text id="bff013"
website-controller
│
├── client         # Feign Clients
├── controller     # REST APIs
├── dto            # Aggregated response DTOs
├── service        # Dashboard logic
├── config         # Feign + Security config
└── util           # Error handling helpers
```

---

## 🧪 Testing

Includes:

* Unit Tests
* Feign Mock Testing
* Integration Testing
* Failure Simulation Tests

### Run Tests

```bash id="bff014"
mvn test
```
