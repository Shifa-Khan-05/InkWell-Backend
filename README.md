🛡️ InkWell Comment Service

---

The **Comment-Service** is a dedicated microservice within the InkWell platform responsible for enabling **user interaction, feedback, and discussions** on blog posts. It supports threaded conversations, moderation workflows, and scalable engagement while maintaining clean separation from other services.

---

## 📌 Overview

The Comment-Service provides a **robust engagement layer** that allows readers to:

* Add comments to posts
* Reply to existing comments (threaded discussions)
* Like/unlike comments
* View comment threads per post
* Support moderation workflows (approve/reject/delete)

It integrates with the **Auth-Service** for user identity and operates independently from the Post-Service using only `postId` references.

---

## 🚀 Key Features

### 💬 Comment Management

* Create, update, and delete comments
* Fetch comments by post
* Nested replies using `parentCommentId`

---

### 🧵 Threaded Conversations

* Supports **hierarchical comment structure**
* Top-level comments + replies
* Efficient retrieval of discussion threads

---

### ❤️ Comment Engagement

* Like/unlike comments
* Track `likesCount` per comment
* Prevent inconsistent engagement updates

---

### 🛡️ Moderation System

* Status-based control:

  * `PENDING`
  * `APPROVED`
  * `REJECTED`
  * `DELETED`

* Admin/moderator actions:

  * Approve comments
  * Reject inappropriate content

---

### 🔗 Microservice Integration

* Uses **OpenFeign** to fetch user details from Auth-Service
* Maintains loose coupling via `userId`

---

### 🌐 Service Discovery

* Registered with **Eureka Server**
* Discoverable by API Gateway and other services

---

## 🛠️ Tech Stack

| Layer         | Technology      |
| ------------- | --------------- |
| Language      | Java 17         |
| Framework     | Spring Boot 3.x |
| Database      | MySQL           |
| ORM           | Spring Data JPA |
| Communication | OpenFeign       |
| Discovery     | Eureka Server   |
| Utilities     | Lombok          |

---

## 🏗️ Architecture (Based on Class Diagram)

### 📦 Core Components

#### 🧾 Comment Entity

Stores:

* `commentId`
* `postId`
* `authorId`
* `parentCommentId` (nullable for top-level comments)
* `content`
* `likesCount`
* `status`
* `createdAt`
* `updatedAt`

---

#### 📂 CommentRepository

Provides:

* `findByPostId()`
* `findByParentCommentId()`
* `findById()`
* `countByPostId()`
* `deleteByCommentId()`

---

#### ⚙️ CommentService

Defines:

* Add comment
* Fetch comments
* Manage replies
* Update/delete comments
* Like/unlike logic
* Moderation actions

---

#### 🔧 CommentServiceImpl

Implements:

* Business logic
* Validation
* Threaded comment handling
* Like system
* Status transitions

---

#### 🌐 CommentResource (Controller)

Handles REST endpoints for:

* Comment CRUD
* Replies
* Moderation
* Engagement

---

## 🗄️ Database Schema

### 📘 Comments Table

| Field           | Description       |
| --------------- | ----------------- |
| commentId       | Primary key       |
| postId          | Associated post   |
| authorId        | User ID           |
| parentCommentId | For replies       |
| content         | Comment text      |
| likesCount      | Engagement count  |
| status          | Moderation state  |
| createdAt       | Created timestamp |
| updatedAt       | Last updated      |

---

## 📡 API Endpoints

### 🔹 Add Comment

```http
POST /comments
```

---

### 🔹 Get Comments by Post

```http
GET /comments/post/{postId}
```

---

### 🔹 Get Comment by ID

```http
GET /comments/{id}
```

---

### 🔹 Get Replies

```http
GET /comments/{id}/replies
```

---

### 🔹 Update Comment

```http
PUT /comments/{id}
```

---

### 🔹 Delete Comment

```http
DELETE /comments/{id}
```

---

### 🔹 Approve Comment

```http
PUT /comments/{id}/approve
```

---

### 🔹 Reject Comment

```http
PUT /comments/{id}/reject
```

---

### 🔹 Like Comment

```http
POST /comments/{id}/like
```

---

### 🔹 Unlike Comment

```http
POST /comments/{id}/unlike
```

---

### 🔹 Get Comment Count

```http
GET /comments/post/{postId}/count
```

---

## 🔄 Inter-Service Communication

### Flow

1. Client requests comments
2. Comment-Service fetches data from DB
3. Extracts `authorId`
4. Calls Auth-Service via Feign
5. Combines data → returns enriched response

---

## ⚙️ Setup & Installation

### ✅ Prerequisites

* Java 17
* Maven
* MySQL
* Eureka Server (port 8761)

---

### 🛢️ Database Setup

```sql
CREATE DATABASE inkwell_comment_db;
```

---

### ⚙️ Configuration

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inkwell_comment_db
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

### ▶️ Run Service

```bash
mvn clean install
mvn spring-boot:run
```

Runs on:

```bash
http://localhost:8083
```

---

## 🔒 Security Considerations

* Trusts JWT validation via API Gateway/Auth-Service
* Ensures only authorized users:

  * Modify their own comments
  * Perform moderation (ADMIN role)

---

## ⚡ Performance & Scalability

### Optimizations

* Indexed queries on `postId`
* Lazy loading for replies
* DTO-based lightweight responses

### Scalability

* Stateless design
* Horizontal scaling supported
* Independent database

---

## 🧪 Testing Strategy

* Unit Tests (Service layer)
* Integration Tests (Repository + DB)
* API Testing (Postman/Swagger)

---

## 📁 Project Structure

```
comment-service
│
├── controller
├── service
├── repository
├── entity
├── dto
├── client
└── config
```

---

## 🚧 Future Enhancements

* 🔔 Notifications for replies
* 🧠 AI-based moderation
* 🔍 Comment search
* 👍 Persistent like tracking (like Post-Service)
* 🧵 Infinite nested threads
* 📊 Engagement analytics

---
