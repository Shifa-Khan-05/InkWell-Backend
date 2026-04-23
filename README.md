# 🏷️ InkWell Taxonomy Service

The **Taxonomy Service** is a foundational microservice in the InkWell Blogging Platform responsible for **content classification, organization, and discoverability**. It manages both **hierarchical categories** and **dynamic tagging**, enabling efficient navigation, filtering, and SEO optimization across the platform.

## 📖 Overview

In a microservices architecture, **content and classification are decoupled**.

The Taxonomy Service acts as a **central metadata engine**, allowing:

* Posts to be categorized in structured hierarchies
* Tags to be assigned dynamically
* Trending topics to be identified
* SEO-friendly URLs to be generated

This ensures a scalable and maintainable content discovery system.

---

## 🚀 Key Features

### 📂 Hierarchical Categories

* Supports **parent-child relationships**
* Enables deep content structuring:

  ```
  Technology → Backend → Spring Boot
  ```
* Each category has a **unique slug** for SEO

---

### 🏷️ Dynamic Tagging System

* Flat structure (no hierarchy)
* Multiple tags per post
* Fast retrieval and filtering

---

### 📊 Trending Tags Engine

* Tracks `postCount` for each tag
* Sorts tags by usage frequency
* Enables **Trending Topics UI**

---

### 🔢 Live Post Counters

* Maintains `postCount` for:

  * Categories
  * Tags
* Updated when posts are created/updated

---

### 🔍 SEO Optimization

* Auto-generates **slug-based URLs**

  ```
  /category/spring-boot
  /tag/microservices
  ```
* Ensures uniqueness to avoid conflicts

---

## 🏗️ Architecture

```text
        ┌──────────────┐
        │ API Gateway  │
        └──────┬───────┘
               │
     ┌─────────▼─────────┐
     │ Taxonomy Service  │
     └─────────┬─────────┘
               │
     ┌─────────▼─────────┐
     │   Post Service    │
     └───────────────────┘
```

### Key Design Principles

* Loose coupling
* Single responsibility
* Scalable metadata management
* Independent database

---

## 🗄️ Data Model

### 📘 Category Entity

| Field            | Description           |
| ---------------- | --------------------- |
| categoryId       | Primary key           |
| name             | Category name         |
| slug             | Unique URL identifier |
| description      | Category details      |
| parentCategoryId | For hierarchy         |
| postCount        | Number of posts       |
| createdAt        | Timestamp             |

---

### 🏷️ Tag Entity

| Field     | Description       |
| --------- | ----------------- |
| tagId     | Primary key       |
| name      | Tag name          |
| slug      | Unique identifier |
| postCount | Usage count       |
| createdAt | Timestamp         |

---

## 📡 API Endpoints

### 📂 Category APIs

| Method | Endpoint                      | Description          |
| ------ | ----------------------------- | -------------------- |
| POST   | `/taxonomy/categories`        | Create category      |
| GET    | `/taxonomy/categories`        | Get all categories   |
| GET    | `/taxonomy/categories/{slug}` | Get category by slug |
| PUT    | `/taxonomy/categories/{id}`   | Update category      |
| DELETE | `/taxonomy/categories/{id}`   | Delete category      |

---

### 🏷️ Tag APIs

| Method | Endpoint                  | Description       |
| ------ | ------------------------- | ----------------- |
| POST   | `/taxonomy/tags`          | Create tag        |
| GET    | `/taxonomy/tags`          | Get all tags      |
| GET    | `/taxonomy/tags/trending` | Get trending tags |
| DELETE | `/taxonomy/tags/{id}`     | Delete tag        |

---

### 🔗 Post-Tag Association

| Method | Endpoint                                | Description         |
| ------ | --------------------------------------- | ------------------- |
| POST   | `/taxonomy/posts/{postId}/tags`         | Assign tags to post |
| DELETE | `/taxonomy/posts/{postId}/tags/{tagId}` | Remove tag          |

---

## 🔄 Service Communication

### Flow

1. Post-Service creates/updates post
2. Calls Taxonomy Service
3. Tags/categories assigned
4. `postCount` updated
5. Response returned

---

## 🔒 Security & Validation

* Ensures **unique slugs**
* Validates parent-child relationships
* Prevents duplicate tags
* Sanitizes inputs for SEO

---

## ⚡ Performance Considerations

* Indexed columns:

  * `slug`
  * `categoryId`
  * `tagId`
* Precomputed `postCount` for fast reads
* Stateless service → horizontally scalable

---

## 📁 Project Structure

```text
taxonomy-service
│
├── controller      # REST APIs
├── service         # Business logic
├── repository      # JPA interfaces
├── entity          # Category & Tag models
├── dto             # Data transfer objects
└── config          # Configurations
```

---
