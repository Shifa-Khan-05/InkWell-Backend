# ✨ InkWell Blogging Platform

InkWell is a **full-stack, enterprise-grade blogging platform** built using **Microservices Architecture**. It enables users to create, publish, and engage with content while ensuring scalability, modularity, and high performance.

The platform is designed to simulate a **real-world production system**, incorporating authentication, content management, payments, notifications, and analytics.

---

# 📌 Table of Contents

* Overview
* System Architecture
* Tech Stack
* Microservices Breakdown
* Core Features
* End-to-End Workflows
* API Gateway & BFF
* Security Architecture
* Setup & Installation
* Database Design
* Performance & Scalability
* Testing Strategy
* Future Enhancements

---

# 🚀 Overview

InkWell follows a **distributed microservices architecture** where each service is responsible for a specific domain.

### 🎯 Objectives

* Build scalable backend systems
* Demonstrate microservices communication
* Implement real-world features (payments, OAuth, notifications)
* Provide seamless frontend experience

---

# 🏗️ System Architecture

```text id="arch001"
React Frontend (UI)
        ↓
Website Controller (BFF) / API Gateway
        ↓
────────────────────────────────────
Auth-Service
Post-Service
Comment-Service
Media-Service
Notification-Service
Newsletter-Service
Payment-Service
Taxonomy-Service
────────────────────────────────────
        ↓
     MySQL Databases
```

---

# 🛠️ Tech Stack

## 💻 Frontend

* React.js
* Tailwind CSS
* Framer Motion

---

## ⚙️ Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Cloud

  * Eureka (Service Discovery)
  * OpenFeign (Communication)
  * API Gateway

---

## 🗄️ Database

* MySQL (Database per service pattern)

---

## 🔐 Security

* JWT Authentication
* Google OAuth2
* Role-Based Access Control (RBAC)

---

## 📧 Email

* JavaMailSender (SMTP)

---

## 💳 Payments

* Razorpay Integration

---

# 🧩 Microservices Breakdown

---

## 🔐 Auth-Service

Handles identity and security.

### Features:

* User registration/login
* JWT token generation
* Google OAuth2 login
* Role management:

```text id="role001"
READER | AUTHOR | ADMIN | PREMIUM
```

---

## 📝 Post-Service

Manages content lifecycle.

### Features:

* Draft → Publish workflow
* Slug generation (SEO-friendly URLs)
* Reading time calculation
* Persistent like/unlike system

---

## 💬 Comment-Service

Handles user discussions.

### Features:

* Add comments
* Nested replies
* Moderation system
* Comment count tracking

---

## 🖼️ Media-Service

Handles media assets.

### Features:

* Image upload (multipart)
* URL generation
* File storage
* Soft delete

---

## 🔔 Notification-Service

Manages alerts.

### Features:

* Like/comment notifications
* Admin broadcasts
* Unread count tracking
* Email alerts

---

## 📧 Newsletter-Service

Handles subscriptions.

### Features:

* Double opt-in subscription
* Welcome emails
* Bulk email broadcasting
* Post notifications

---

## 💳 Payment-Service

Handles premium subscriptions.

### Features:

* Razorpay order creation
* Payment verification
* Role upgrade via Auth-Service

---

## 🏷️ Taxonomy-Service

Handles classification.

### Features:

* Hierarchical categories
* Tag system
* Trending tags
* SEO-friendly slugs

---

## 🌐 Website Controller (BFF)

Acts as frontend gateway.

### Features:

* Aggregates data from all services
* Admin dashboard analytics
* Simplifies frontend API calls
* Handles service failures gracefully

---

# 📡 Core Features

* ✍️ Create, edit, and publish blogs
* ❤️ Like/unlike system with persistence
* 💬 Threaded comment system
* 🖼️ Image upload and management
* 🔔 Real-time notifications
* 📧 Newsletter subscription system
* 💳 Premium subscription via Razorpay
* 🏷️ Categories and tags
* 📊 Admin analytics dashboard

---

# 🔄 End-to-End Workflows

---

## 📝 Post Publishing Flow

```text id="flow001"
Frontend → Media-Service → Post-Service → Taxonomy-Service → Notification-Service → Newsletter-Service
```

### Steps:

1. User uploads image
2. Media-Service returns URL
3. Post-Service creates post
4. Tags assigned
5. Notifications triggered
6. Subscribers notified

---

## 💳 Payment Flow

```text id="flow002"
Frontend → Payment-Service → Razorpay → Payment-Service → Auth-Service
```

### Steps:

1. Order created
2. User pays via Razorpay
3. Payment verified
4. Auth-Service upgrades role

---

## 💬 Comment Flow

```text id="flow003"
Frontend → Comment-Service → Notification-Service
```

---

# 🔐 Security Architecture

* JWT for stateless authentication
* OAuth2 for social login
* RBAC for access control
* Secure payment verification
* Environment variable protection
---

## 🛢️ Database Setup

```sql id="db001"
CREATE DATABASE inkwell_auth;
CREATE DATABASE inkwell_post;
CREATE DATABASE inkwell_comment;
CREATE DATABASE inkwell_media;
CREATE DATABASE inkwell_notification;
CREATE DATABASE inkwell_newsletter;
CREATE DATABASE inkwell_payment;
CREATE DATABASE inkwell_taxonomy;
```

---

# 🧪 Testing Strategy

* Unit Testing (JUnit, Mockito)
* API Testing (Postman)
* Frontend Testing (React Testing Library, Cypress, Jest)

---

# ⚡ Performance & Scalability

* Stateless services
* Independent deployment
* Database per service
* Feign communication
* API Gateway routing
* Graceful degradation

---
