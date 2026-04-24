# 💳 InkWell Payment Service

The **Payment Service** is a critical microservice within the InkWell Blogging Platform responsible for managing **premium subscriptions, secure payment processing, and user role upgrades**. It integrates with **Razorpay** for payment gateway operations and communicates with the **Auth-Service** using **OpenFeign** to upgrade users to premium access after successful payment verification.

---

## 📌 Overview

In the InkWell ecosystem, premium features such as exclusive content, advanced writing tools, and enhanced visibility require a secure and reliable payment system.

The Payment Service acts as the **financial transaction engine** by:

* Creating secure payment orders
* Verifying payment authenticity
* Maintaining transaction records
* Upgrading user roles after successful payments
* Ensuring secure service-to-service communication

This service keeps payment logic completely separated from the Auth-Service and Post-Service, following proper **microservices architecture principles**.

---

## 🚀 Key Features

### 💰 Razorpay Order Creation

* Generates secure and unique Razorpay Order IDs
* Supports dynamic payment amounts
* Prevents duplicate transactions

---

### 🔐 Payment Verification

* Validates:

  * `razorpay_payment_id`
  * `razorpay_order_id`
  * `razorpay_signature`

* Prevents fake or tampered transactions

---

### 🔗 Auth-Service Integration

* Uses **OpenFeign Client**
* Automatically upgrades user role after successful payment:

```id="pmt001"
ROLE_USER → ROLE_PREMIUM
```

---

### 📄 Transaction Persistence

* Stores payment logs in MySQL
* Maintains transaction history for:

  * Auditing
  * Refund handling
  * Admin reporting

---

### ⚠️ Error Handling

* Handles:

  * Payment failures
  * Signature mismatch
  * Gateway timeout
  * Feign communication failure

---

### 🛡️ Secure Internal Communication

* Service discovery using Eureka
* Secure inter-service communication within the cluster

---

## 🛠️ Tech Stack

| Layer           | Technology        |
| --------------- | ----------------- |
| Language        | Java 17           |
| Framework       | Spring Boot 3.x   |
| Database        | MySQL             |
| ORM             | Spring Data JPA   |
| Payment Gateway | Razorpay Java SDK |
| Communication   | OpenFeign         |
| Discovery       | Eureka Client     |
| Utilities       | Lombok            |

---

## 🏗️ System Architecture

```text id="pmt002"
Frontend → Payment Service → Razorpay
                    ↓
               Auth-Service
                    ↓
              User Role Upgrade
```

---

## 🗄️ Data Model

### 📘 Transaction Entity

| Field             | Description                |
| ----------------- | -------------------------- |
| transactionId     | Primary key                |
| userId            | User making payment        |
| amount            | Payment amount             |
| razorpayOrderId   | Gateway order ID           |
| razorpayPaymentId | Payment confirmation ID    |
| status            | SUCCESS / FAILED / PENDING |
| paymentMethod     | Card / UPI / Net Banking   |
| createdAt         | Transaction timestamp      |

---

## 📡 API Endpoints

---

### 🔹 Create Payment Order

```http id="pmt003"
POST /payments/create-order
```

### Request Body

```json id="pmt004"
{
  "amount": 499
}
```

### Description

Creates a new Razorpay order and returns the generated Order ID.

---

### 🔹 Verify Payment

```http id="pmt005"
POST /payments/verify
```

### Request Body

```json id="pmt006"
{
  "razorpay_payment_id": "pay_test_123",
  "razorpay_order_id": "order_test_456",
  "razorpay_signature": "sig_abc_789",
  "userId": "21"
}
```

### Description

Verifies the payment signature and upgrades the user to:

```id="pmt007"
ROLE_PREMIUM
```

---

## 🔄 Payment Workflow (Important 🔥)

### Step-by-Step Flow

### 1️⃣ Frontend Requests Order

React frontend sends request to:

```id="pmt008"
/payments/create-order
```

---

### 2️⃣ Payment Service Creates Order

* Razorpay SDK creates order
* Returns:

```id="pmt009"
order_id
```

---

### 3️⃣ User Completes Payment

* Razorpay Checkout opens
* User pays via:

  * UPI
  * Card
  * Net Banking

---

### 4️⃣ Frontend Sends Verification

Frontend sends:

* payment_id
* order_id
* signature
* userId

to:

```id="pmt010"
/payments/verify
```

---

### 5️⃣ Backend Verifies Signature

If valid:

✅ Payment success

If invalid:

❌ Reject transaction

---

### 6️⃣ Feign Client Calls Auth-Service

```java id="pmt011"
authClient.upgradeUser(userId);
```

---

### 7️⃣ User Becomes Premium 🎉

```id="pmt012"
ROLE_PREMIUM
```

---

## ⚙️ Configuration

### application.properties

```properties id="pmt013"
server.port=8089
spring.application.name=PAYMENT-SERVICE

# Razorpay Credentials
razorpay.key.id=rzp_test_your_key
razorpay.key.secret=your_secret_key

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/inkwell_payment
spring.datasource.username=root
spring.datasource.password=your_password

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Auth Service
AUTH_SERVICE_URL=http://AUTH-SERVICE
```

---

## 🚦 Setup & Installation

### ✅ Prerequisites

* Java 17
* Maven
* MySQL
* Razorpay Test Account
* Eureka Server running on `8761`

---

### 🛢️ Database Setup

```sql id="pmt014"
CREATE DATABASE inkwell_payment;
```

---

### ▶️ Run Application

```bash id="pmt015"
mvn clean install
mvn spring-boot:run
```

---

## 🧪 Testing

This service includes unit testing using:

* JUnit 5
* Mockito

### Run Tests

```bash id="pmt016"
mvn test
```

Tests cover:

* Order creation logic
* Payment verification
* Signature validation
* Feign client interaction

---

## 📁 Project Structure

```text id="pmt017"
payment-service
│
├── client         # Feign Client
├── config         # Razorpay config
├── controller     # REST APIs
├── dto            # Request/Response DTOs
├── entity         # Transaction entity
├── repository     # JPA layer
└── service        # Business logic
```

---

## 🔒 Security Considerations

* Signature verification mandatory
* Secrets stored in `.env` / environment variables
* Never expose Razorpay secret key
* Secure Feign communication
* Admin-level audit logs recommended

---

## ⚡ Future Enhancements

* Refund handling
* Subscription renewals
* Invoice generation
* Payment analytics dashboard
* Webhook integration from Razorpay
* Kafka event-driven payment confirmation

---
