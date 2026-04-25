# 🔔 InkWell Notification Service

The **Notification-Service** is a critical microservice within the InkWell Blogging Platform responsible for managing **real-time user alerts, engagement notifications, and system-wide communication**. It ensures that authors, readers, and administrators stay informed about platform activities such as likes, comments, approvals, and important announcements.

---

## 📌 Overview

In a modern microservices architecture, user engagement should not be tightly coupled with Post-Service or Comment-Service. Instead, the Notification-Service acts as a **central alert engine** that handles:

* In-app notifications
* Email alerts
* System broadcasts
* Unread notification tracking
* Notification cleanup and inbox maintenance

This service improves user engagement and platform responsiveness while maintaining clean service separation.

---

## 🚀 Key Features

### 🔄 Activity Tracking

* Captures events triggered by:

  * Post-Service
  * Comment-Service
  * Admin actions

Examples:

* New comment on your post
* Someone liked your article
* Post approval notification
* System maintenance alerts

---

### 📥 In-App Notifications

* Stores notifications in database
* Supports persistent inbox system
* Users can view history of alerts

---

### 📧 Email Integration

* Uses **JavaMailSender**
* Sends external alerts for:

  * Important updates
  * Premium notifications
  * System announcements

---

### 📢 Admin Broadcast System

* Admins can send:

```text id="ntf001"
SYSTEM ALERTS
```

to all users at once

Examples:

* Maintenance downtime
* Platform updates
* New feature announcements

---

### 🧹 Inbox Maintenance

* Mark notifications as read
* Mark all as read
* Delete processed/read notifications
* Prevent database overload

---

### 🔢 Unread Count Tracking

Used for:

🔔 Navbar badge count

Example:

```text id="ntf002"
5 unread notifications
```

---

## 🛠️ Tech Stack

| Layer     | Technology               |
| --------- | ------------------------ |
| Language  | Java 17                  |
| Framework | Spring Boot 3.2.5        |
| Database  | MySQL                    |
| ORM       | Spring Data JPA          |
| Email     | Spring Boot Starter Mail |
| Discovery | Eureka Client            |
| Utilities | Lombok                   |

---

## 🏗️ System Architecture

```text id="ntf003"
Post Service ─┐
              │
Comment Service ──→ Notification Service → User Inbox
              │
 Admin Actions ─┘
                     ↓
                Email Alerts
```

---

## 🗄️ Data Model

### 📘 Notification Entity

| Field       | Description             |
| ----------- | ----------------------- |
| id          | Primary key             |
| recipientId | User receiving alert    |
| actorId     | User triggering event   |
| type        | Notification category   |
| message     | Notification content    |
| isRead      | Inbox read status       |
| relatedId   | Related post/comment ID |
| createdAt   | Timestamp               |

---

## 🏷️ Notification Types

Examples:

* `COMMENT`
* `LIKE`
* `POST_APPROVAL`
* `SYSTEM`
* `FOLLOW`
* `NEWSLETTER`

---

## 📡 API Endpoints

---

### 🔹 Create Notification

```http id="ntf004"
POST /notifications/send
```

Creates and stores a new notification.

---

### 🔹 Get User Notifications

```http id="ntf005"
GET /notifications/user/{id}
```

Fetch all notifications for a user.

---

### 🔹 Get Unread Count

```http id="ntf006"
GET /notifications/user/{id}/unread-count
```

Used for bell icon badge.

---

### 🔹 Mark One as Read

```http id="ntf007"
PUT /notifications/{id}/read
```

Updates a single notification.

---

### 🔹 Mark All as Read

```http id="ntf008"
PUT /notifications/user/{id}/read-all
```

Bulk read operation.

---

### 🔹 Cleanup Read Notifications

```http id="ntf009"
DELETE /notifications/user/{id}/cleanup
```

Deletes all read notifications.

---

## 🔄 Notification Flow (Important 🔥)

### Example: Comment Notification

### 1️⃣ Reader comments on post

↓

### 2️⃣ Comment-Service triggers Notification-Service

↓

### 3️⃣ Notification stored in DB

↓

### 4️⃣ Optional email sent

↓

### 5️⃣ Author sees alert in Inbox 🔔

---

## ⚙️ Configuration

### application.properties

```properties id="ntf010"
server.port=8084
spring.application.name=notification-service

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/inkwell_notification_db
spring.datasource.username=root
spring.datasource.password=your_password

# Mail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

---

## 🚦 Setup & Installation

### ✅ Prerequisites

* Java 17
* Maven
* MySQL
* Eureka Server running on `8761`
* Gmail App Password for SMTP

---

### 🛢️ Database Setup

```sql id="ntf011"
CREATE DATABASE inkwell_notification_db;
```

---

### ▶️ Run Application

```bash id="ntf012"
mvn clean install
mvn spring-boot:run
```

---

## 🧪 Testing

Includes unit testing using:

* JUnit 5
* Mockito

### Run Tests

```bash id="ntf013"
mvn test
```

Tests cover:

* Notification creation
* Unread count logic
* Mark-as-read flow
* Cleanup operations
* Email trigger validation

---

## 📁 Project Structure

```text id="ntf014"
notification-service
│
├── controller      # REST APIs
├── entity          # Notification entity
├── repository      # JPA layer
├── service         # Business logic
├── config          # Mail & app configuration
└── dto             # Request/Response DTOs
```

---

## 🔒 Security Considerations

* Validate recipient ownership
* Protect admin broadcast endpoints
* Avoid duplicate notifications
* Secure email credentials using `.env`

---
