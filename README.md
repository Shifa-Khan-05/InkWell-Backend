# 🖼️ InkWell Media Service

The **Media-Service** is a dedicated microservice in the InkWell platform responsible for **handling all media assets** such as images (avatars, post covers, inline media). It ensures efficient storage, retrieval, and delivery of binary data while keeping other services lightweight and focused.

---

## 📌 Overview

In a microservices architecture, storing files directly inside services like Post-Service leads to performance and scalability issues.

The Media-Service solves this by:

* Handling **file uploads separately**
* Generating **public URLs for assets**
* Managing **storage and retrieval efficiently**
* Providing **secure and scalable media access**

---

## 🚀 Core Features

### 📤 Multipart File Upload

* Supports formats:

  * JPG, PNG, WEBP, AVIF
* Handles `multipart/form-data` requests efficiently

---

### 💾 Local File Storage

* Files stored in structured directory:

```bash
uploads/media/
```

* Uses **Java NIO** for file operations

---

### 🔗 URL Handshake System

* Generates public URL instantly after upload
* Example:

```bash
http://localhost:8087/media/display/12345_image.jpg
```

* Used directly by Post-Service

---

### 🧹 Soft Delete Mechanism

* Uses `isDeleted` flag instead of physical deletion
* Prevents broken references in Post-Service

---

### 🛡️ Admin Controls

* View all media
* Moderate uploaded content
* Manage assets globally

---

### 🖥️ Dynamic Image Streaming

* Streams images via endpoint
* Does NOT expose actual file path

---

## 🛠️ Tech Stack

| Layer         | Technology        |
| ------------- | ----------------- |
| Language      | Java 17           |
| Framework     | Spring Boot 3.2.5 |
| Database      | MySQL             |
| ORM           | Spring Data JPA   |
| File Handling | Java NIO          |
| Discovery     | Eureka Client     |
| Utilities     | Lombok            |

---

## 🏗️ Architecture

```text
Frontend → Media-Service → File System
                    ↓
              Public URL → Post-Service
```

---

## 📡 API Endpoints

### 📤 Upload Media

```http
POST /media/upload
```

**Request:**

* file (image)
* uploaderId
* altText

**Response:**

* Media metadata + public URL

---

### 📥 Fetch Media

| Method | Endpoint                | Description            |
| ------ | ----------------------- | ---------------------- |
| GET    | `/media/all`            | Get all assets (Admin) |
| GET    | `/media/uploader/{id}`  | Get user uploads       |
| GET    | `/media/display/{name}` | Stream image           |
| GET    | `/media/detail/{id}`    | Get metadata           |

---

### ✏️ Update / Delete

| Method | Endpoint          | Description     |
| ------ | ----------------- | --------------- |
| PUT    | `/media/{id}/alt` | Update alt text |
| DELETE | `/media/{id}`     | Soft delete     |

---

## 🔄 Media Upload Flow (Important 🔥)

1. User selects image in frontend
2. Frontend sends request to Media-Service
3. Media-Service:

   * Saves file
   * Generates URL
4. URL returned to frontend
5. Frontend sends URL to Post-Service
6. Post stored with image URL

👉 This keeps Post-Service lightweight

---

## ⚙️ Setup & Installation

### ✅ Prerequisites

* Java 17
* Maven
* MySQL
* Eureka Server (8761)

---

### 🛢️ Database Setup

```sql
CREATE DATABASE inkwell_media;
```

---

### ⚙️ Configuration

```properties
server.port=8087
spring.application.name=media-service

spring.datasource.url=jdbc:mysql://localhost:3306/inkwell_media
spring.datasource.username=root
spring.datasource.password=your_password

spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```
---

## 📁 Project Structure

```text
media-service
│
├── controller      # REST APIs
├── entity          # Media entity
├── repository      # JPA layer
├── service         # Business logic
├── config          # Configurations
```

---

## 🔒 Security Considerations

* Validate file types
* Limit file size
* Prevent path traversal
* Restrict admin endpoints

---

## ⚡ Performance & Scalability

* Lightweight URL-based linking
* No binary storage in Post-Service
* Ready for:

  * AWS S3
  * Cloud storage

---
