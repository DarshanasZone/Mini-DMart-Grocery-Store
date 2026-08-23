# 🎯 Mini-DMart — Technical Interview Q&A & Concept Guide

A complete guide to help you explain the Mini-DMart architecture, design choices, Spring Boot concepts, and database operations during your technical interview.

---

## 🌟 1. Project Elevator Pitch (How to introduce your project in 60 seconds)

> *"Mini-DMart is a full-stack online grocery shopping web application inspired by DMart retail operations. On the backend, I built a modular Spring Boot 3 REST API in Java 21 with Spring Data JPA, Hibernate, PostgreSQL, and Spring Security with stateless JWT and RBAC. On the frontend, I used responsive HTML5, CSS3, and Vanilla JavaScript with the Fetch API.*
>
> *Key business workflows include real-time grocery catalog browsing, cart operations with delivery threshold rules, transactional order placement with atomic inventory stock reduction, order lifecycle tracking (`PLACED` to `DELIVERED`), self-service order cancellations with stock restoration, and post-purchase Return and Exchange request workflows. I also built a comprehensive Admin Management Console with live revenue KPI metrics, category & product management, approval workflows, and an immutable audit log trail."*

---

## 💡 2. Core Technical Questions & Answers

### Q1: What architecture did you follow in this project?
**Answer**:
*"I followed a clean **Layered (N-Tier) Architecture** with clear separation of concerns:*
1. ***Controller Layer***: Handles HTTP requests, endpoint routing, and request/response mapping.
2. ***DTO Layer***: Decouples the API contract from the internal database entities, preventing over-posting and circular JSON serialization issues.
3. ***Service Layer***: Contains all business logic, invariant enforcement, and `@Transactional` state mutations.
4. ***Repository Layer***: Extends Spring Data JPA repositories to perform CRUD and custom queries.
5. ***Database Layer***: PostgreSQL database ensuring ACID guarantees."*

---

### Q2: How did you implement authentication and authorization?
**Answer**:
*"I implemented **stateless JWT authentication** using Spring Security 6 and JJWT:*
1. *When a user logs in via `POST /api/auth/login`, Spring Security verifies the BCrypt password hash.*
2. *Upon successful verification, `JwtService` generates a signed token containing the user's email, role (`ROLE_CUSTOMER` or `ROLE_ADMIN`), issued timestamp, and 24-hour expiration.*
3. *The client browser stores this token in `localStorage` and includes it in the `Authorization: Bearer <token>` header for subsequent requests.*
4. *On every protected request, `JwtAuthenticationFilter` intercepts the request, validates the signature, extracts the authorities, and populates the `SecurityContextHolder`.*
5. *Admin endpoints are protected using `@PreAuthorize("hasRole('ADMIN')")` and `SecurityConfig` route rules."*

---

### Q3: How do you prevent race conditions and maintain stock integrity during checkout?
**Answer**:
*"I used **Spring `@Transactional` boundaries** in `OrderService.placeOrder()`:*
- *First, it validates the cart and checks if each requested product has sufficient stock (`stockQuantity >= cartQty`).*
- *If sufficient, it decrements the inventory atomically and creates the order and item records.*
- *If any item has insufficient stock, an `InsufficientStockException` is thrown, and Spring automatically rolls back the entire transaction so no stock is deducted and no partial order is saved.*
- *Similarly, when an order is cancelled or a return is approved by the admin, the exact ordered quantities are atomically added back to the product's inventory."*

---

### Q4: Why did you use DTOs instead of exposing JPA Entities directly?
**Answer**:
*"Using DTOs provides multiple critical advantages:*
1. ***Security & Encapsulation***: Prevents mass assignment / over-posting attacks where malicious users send unwanted entity fields (e.g. attempting to modify `role` or `id`).
2. ***Eliminates Circular Reference Errors***: JPA bidirectional associations (like `Order` ➔ `OrderItem` ➔ `Order`) cause infinite JSON loops during Jackson serialization if entities are returned directly.
3. ***Performance & Clean Contracts***: Lets the API return computed view properties (such as `canCancel`, `canReturnOrExchange`, and formatted dates) without polluting the database schema."*

---

### Q5: How does the Return and Exchange workflow work?
**Answer**:
*"The post-purchase workflow is designed with business validation and inventory adjustment:*
- ***Return Flow***: A customer can request a return on any `DELIVERED` order. The request enters `REQUESTED` status. When the store admin clicks 'Approve', `ReturnService` updates the status to `APPROVED` and automatically restores the ordered item's stock back to store inventory.
- ***Exchange Flow***: The customer picks their original ordered item and chooses a replacement product. When the store admin approves the exchange, `ExchangeService` validates that the new replacement product is in stock, decrements 1 unit of the new product, and increments 1 unit of the old product."*

---

### Q6: How does the Audit Logging mechanism work?
**Answer**:
*"Every major administrative and transactional operation (such as placing an order, updating order status, approving a return, or modifying products) calls `AuditService.logAction()`.*
*This records the user's email, action type (e.g., `ORDER_PLACED`, `ORDER_STATUS_UPDATED`, `RETURN_APPROVED`), target entity name, entity ID, description details, and a UTC timestamp in the `audit_logs` PostgreSQL table for accountability and auditing."*

---

### Q7: How is exception handling handled across the application?
**Answer**:
*"I implemented a centralized `@RestControllerAdvice` class called `GlobalExceptionHandler`.*
*It intercepts custom domain exceptions (`ResourceNotFoundException`, `InsufficientStockException`, `BadRequestException`, `UnauthorizedException`) as well as Bean Validation errors, and transforms them into a uniform `ApiErrorResponse` JSON payload containing `timestamp`, `status`, `error`, `message`, and `path`."*

---

## 📋 3. Key Concepts Checklist for Revision

| Concept | Why/Where It Was Used |
| :--- | :--- |
| **`@Entity` & `@Table`** | Mapped 10 relational tables in PostgreSQL (`users`, `products`, `orders`, etc.) |
| **`@OneToMany` / `@ManyToOne`** | Structured relationships between Orders and OrderItems, Categories and Products |
| **`@Transactional`** | Guaranteed atomic operations during checkout, cancellation, returns, and exchanges |
| **`BCryptPasswordEncoder`** | Irreversible cryptographic hashing of user passwords with 10 salt rounds |
| **`JwtAuthenticationFilter`** | Once-per-request filter validating JWT tokens before reaching controller endpoints |
| **`@RestControllerAdvice`** | Global interceptor transforming exceptions into structured HTTP response payloads |
| **`DataInitializer`** | `CommandLineRunner` bootstrapping demo credentials, categories, and initial products |
