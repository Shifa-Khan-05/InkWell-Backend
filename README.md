🛡️ InkWell Post Service

---

The **Post-Service** is a core microservice of the InkWell blogging platform. It manages the **entire content lifecycle**, from drafting stories to publishing them for public engagement.

---

## 📌 Overview

The Post-Service powers the storytelling experience in InkWell by enabling:

* Draft creation and publishing workflows
* SEO-friendly post management
* Reader engagement (likes system)
* Integration with Auth-Service for author details

It plays a critical role in delivering a seamless writing and reading experience.

---

## 🚀 Key Features

### 📝 Draft & Publish Workflow

* Authors can create posts as **DRAFT**
* Publish posts to make them visible in the public feed
* Supports iterative content creation

### 🧠 Intelligent Metadata

* Auto-generates **SEO-friendly slugs**
* Calculates **estimated reading time** based on word count

### ❤️ Smart Engagement System

* Ensures **one-user-one-like rule**
* Supports **like/unlike toggle**
* Prevents duplicate likes using database constraints

### 🔗 Author Enrichment

* Uses **Feign Client** to fetch author details from Auth-Service
* Maps `authorId` → real user identity

### 🌐 Slug-Based Routing

* Clean and SEO-friendly URLs:

```id="n7g1jv"
/posts/my-first-story
```

---

## 🛠️ Tech Stack

| Layer         | Technology        |
| ------------- | ----------------- |
| Language      | Java 17           |
| Framework     | Spring Boot 3.2.5 |
| Database      | MySQL             |
| ORM           | Hibernate (JPA)   |
| Auditing      | Hibernate Envers  |
| Communication | OpenFeign         |
| Discovery     | Eureka Server     |

---

## 🏗️ Architecture Highlights

* Microservice-based design
* Service discovery via Eureka
* Inter-service communication via Feign
* Transaction-safe engagement system

---

## ⚙️ Technical Implementation

### ❤️ Engagement Persistence

To ensure **data integrity and consistency**, likes are stored in a separate table:

```java id="l2m9pw"
@Table(name = "post_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class PostLike {
    // fields, getters, setters
}
```

### 🔒 Key Design Principles

* **Atomic Transactions** using `@Transactional`
* Prevents duplicate likes
* Ensures consistency between:

  * Like count
  * Individual user engagement

---

## 📡 API Endpoints

| Method   | Endpoint                           | Description                        |
| -------- | ---------------------------------- | ---------------------------------- |
| **POST** | `/posts/create`                    | Create a new post (default: DRAFT) |
| **GET**  | `/posts/published`                 | Get all published posts            |
| **GET**  | `/posts/slug/{slug}?userId={id}`   | Get post details + like status     |
| **POST** | `/posts/{postId}/like?userId={id}` | Toggle like/unlike                 |

---

## ⚙️ Setup & Installation

### 1️⃣ Prerequisites

* Java 17
* Maven
* MySQL
* Eureka Server running on `8761`

---

### 2️⃣ Database Configuration

Update `application.properties`:

```properties id="p8y2wk"
spring.datasource.url=jdbc:mysql://localhost:3306/inkwell_post
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

### 3️⃣ Important Migration Note

If migrating from older versions:

```sql id="k4twlq"
DROP TABLE IF EXISTS post_likes;
```

---

### 4️⃣ Run the Service

```bash id="n2fjg7"
mvn clean install
mvn spring-boot:run
```

---

## 🔗 Service Dependencies

* **Auth-Service** → Fetch author details
* **Eureka Server** → Service discovery

---

## 🧪 Example Request

```bash id="o3ql2n"
curl -X POST http://localhost:8082/posts/create \
-H "Content-Type: application/json" \
-d '{"title":"My First Blog","content":"Hello InkWell!"}'
```

---

## 📁 Project Structure

```id="b7x2mr"
src/main/java/com/inkwell/post
│
├── controller      # REST APIs
├── service         # Business logic
├── repository      # JPA repositories
├── entity          # Database models
├── dto             # Request/Response objects
├── client          # Feign clients
└── config          # Configurations
```

---

## 🔒 Design Considerations

* Data consistency using transactions
* Scalable microservice communication
* Clean separation of concerns
* SEO optimization via slug system

---

## 🚧 Future Enhancements

* Comments system
* Bookmarking feature
* Trending algorithm
* Tag/category support
* Search & filtering

---
