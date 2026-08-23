# 🛒 Mini-DMart — Full-Stack Online Grocery Shopping Application

A production-style, full-stack grocery e-commerce web application inspired by DMart retail operations. Built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **Spring Security (JWT + RBAC)**, and a responsive **HTML5/CSS3/Vanilla JavaScript** frontend.

---

## 📋 Table of Contents
- [Project Overview](#-project-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Architecture & Design](#-architecture--design)
- [Database Schema (ER Model)](#-database-schema-er-model)
- [Quick Start Guide](#-quick-start-guide)
- [Test Credentials & Demo Accounts](#-test-credentials--demo-accounts)
- [API Endpoints Overview](#-api-endpoints-overview)
- [Project Structure](#-project-structure)
- [Documentation Index](#-documentation-index)

---

## 🌟 Project Overview

**Mini-DMart** delivers a complete grocery shopping experience covering the entire retail lifecycle:
1. **Storefront Browsing**: Customers browse fresh produce, dairy, bakery, and grocery staples with live search and department filtering.
2. **Cart & Fulfillment**: Customers manage cart quantities, view free delivery eligibility (> ₹500), and choose between **Home Delivery** and **Store Pickup**.
3. **Inventory & Order Management**: Atomic stock reduction on checkout, order status progression (`PLACED` ➔ `CONFIRMED` ➔ `SHIPPED` ➔ `DELIVERED`), and order cancellation with automatic stock rollback.
4. **Post-Purchase Operations**: Return requests and item exchange workflows with automatic stock restock/reservation upon admin approval.
5. **Admin Console**: Store management, category CRUD, product & stock control, order fulfillment, and immutable chronological audit logging.

---

## ✨ Key Features

### 👤 Customer Experience
* **Authentication & RBAC**: Customer registration and secure login with stateless JWT tokens stored in `localStorage`.
* **Live Catalog & Search**: Real-time debounce search across names and categories; department filtering ribbon.
* **Product Details**: Dedicated product page with quantity selectors and stock limit validation.
* **Shopping Cart**: Dynamic quantity increment/decrement, subtotal calculation, and free delivery threshold indicator (> ₹500).
* **Flexible Fulfillment**: Support for **Home Delivery** (with address input) and **Store Pickup** (with pickup slot scheduling).
* **Order History & Tracking**: Chronological order history with itemized breakdown and self-service order cancellation.
* **Returns & Exchanges**: Submit return or product replacement requests directly from delivered orders.

### ⚙️ Admin Management Console
* **Performance Dashboard**: Real-time sales KPI cards (Total Revenue, Total Orders, Active Catalog Items, Pending Approvals).
* **Category Management**: Create, edit, and deactivate grocery department classifications.
* **Products & Stock Control**: Add new products, update prices, adjust inventory quantities, and toggle catalog visibility.
* **Order Fulfillment**: Track and update order statuses (`PLACED` ➔ `CONFIRMED` ➔ `SHIPPED` ➔ `DELIVERED`).
* **Return & Exchange Approvals**: One-click approval/rejection with automatic inventory restocking and replacement unit reservation.
* **Audit Trail**: Chronological, immutable audit log recording operator emails, entity targets, actions, and timestamps.

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend Framework** | Java 21, Spring Boot 3.3+ (Spring Web, Spring Data JPA, Hibernate, Spring Security 6) |
| **Security & Auth** | JSON Web Tokens (JJWT 0.12), BCrypt Password Hashing, Role-Based Access Control (RBAC) |
| **Database** | PostgreSQL 16+ (`mini_dmart`) via HikariCP Connection Pool |
| **API Documentation** | OpenAPI 3.0 / Swagger UI (SpringDoc OpenAPI Starter) |
| **Frontend** | Pure HTML5, Modern Modular CSS3, Vanilla JavaScript (ES6+ Fetch API, async/await) |
| **Build & Tooling** | Apache Maven, Lombok, Jakarta Bean Validation |

---

## 🏛️ Architecture & Design

Mini-DMart follows a strict **Layered Architecture** with unidirectional data flow and strong separation of concerns:

```
[ Frontend: HTML5 / CSS3 / Vanilla JS ]
                   │
                   ▼ (HTTP / JSON via Fetch API)
[ Security Filter Chain: JwtAuthenticationFilter + RBAC ]
                   │
                   ▼
[ REST Controller Layer: Auth, Products, Cart, Orders, Returns, Exchanges, Audit ]
                   │
                   ▼ (DTOs & View Models)
[ Business Service Layer: @Transactional Business Logic, Invariants, Stock Calculations ]
                   │
                   ▼
[ Data Access Layer: Spring Data JPA Repositories ]
                   │
                   ▼
[ Relational Database: PostgreSQL (mini_dmart) ]
```

### Key Design Principles:
1. **Layered Decoupling**: Controllers handle HTTP protocol details; Services enforce business rules; Repositories manage database persistence.
2. **DTO Encapsulation**: Database entities are never exposed directly to clients; dedicated Request and Response DTOs prevent over-posting and JSON serialization cycles.
3. **Transactional Integrity**: All multi-step operations (order checkout, cancellations, returns, exchanges) execute within `@Transactional` boundaries to guarantee ACID compliance.
4. **Stateless Security**: REST endpoints validate cryptographically signed JWT tokens without server-side HTTP sessions.

---

## 🗄️ Database Schema (ER Model)

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    USERS     │       │  CATEGORIES  │       │   PRODUCTS   │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │       │ id (PK)      │───┐   │ id (PK)      │
│ name         │       │ name         │   └──<│ category_id  │
│ email        │       │ description  │       │ name         │
│ password     │       │ active       │       │ price        │
│ role         │       └──────────────┘       │ stock_qty    │
└──────┬───────┘                              │ image_url    │
       │                                      │ active       │
       ├───────────────────┬──────────────┐   └──────┬───────┘
       │                   │              │          │
       ▼                   ▼              ▼          ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐
│    CARTS     │   │    ORDERS    │   │      CART_ITEMS      │
├──────────────┤   ├──────────────┤   ├──────────────────────┤
│ id (PK)      │   │ id (PK)      │   │ id (PK)              │
│ user_id (FK) │   │ user_id (FK) │   │ cart_id (FK)         │
└──────┬───────┘   └──────┬───────┘   │ product_id (FK)      │
       │                  │           │ quantity             │
       ▼                  ▼           └──────────────────────┘
┌──────────────┐   ┌──────────────┐
│  CART_ITEMS  │   │ ORDER_ITEMS  │
├──────────────┤   ├──────────────┤
│ (as above)   │   │ id (PK)      │
└──────────────┘   │ order_id(FK) │
                   │ product_id   │
                   │ quantity     │
                   │ price        │
                   │ subtotal     │
                   └──────────────┘
```

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **Java JDK 21** or higher (`java -version`)
- **Maven 3.8+** (`mvn -v`)
- **PostgreSQL 14+** running on `localhost:5432`

### 2. Database Initialization
Create the database in PostgreSQL:
```sql
CREATE DATABASE mini_dmart;
```

### 3. Configure Database Credentials
Edit [`src/main/resources/application.properties`](src/main/resources/application.properties) if your credentials differ from the defaults:
```properties
server.port=8090
spring.datasource.url=jdbc:postgresql://localhost:5432/mini_dmart
spring.datasource.username=postgres
spring.datasource.password=root
```

### 4. Build and Run
```bash
# Compile and package
mvn clean compile

# Launch the application
mvn spring-boot:run
```

### 5. Access Application URLs
* **Storefront**: [http://localhost:8090/](http://localhost:8090/)
* **Customer / Admin Login**: [http://localhost:8090/login.html](http://localhost:8090/login.html)
* **Admin Console**: [http://localhost:8090/admin/dashboard.html](http://localhost:8090/admin/dashboard.html)
* **Interactive Swagger UI**: [http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html)

---

## 🔑 Test Credentials & Demo Accounts

The application automatically seeds demo accounts, 7 departments, and 18 grocery products upon startup:

| Role | Email | Password | Access & Responsibilities |
| :--- | :--- | :--- | :--- |
| **Store Admin** | `admin@dmart.com` | `Admin@123` | Full administrative control, catalog, stock, order statuses, return/exchange approvals, audit log inspection |
| **Customer** | `customer@dmart.com` | `Customer@123` | Storefront browsing, cart operations, checkout, order history, return & exchange requests |

> **Tip**: The login page at `http://localhost:8090/login.html` provides **1-Click Demo Login** buttons for instant testing.

---

## 📡 API Endpoints Overview

| Module | Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/register` | Public | Register a new customer account |
| **Auth** | `POST` | `/api/auth/login` | Public | Authenticate user and receive JWT token |
| **Products** | `GET` | `/api/customer/products` | Public | List all active grocery products |
| **Products** | `GET` | `/api/customer/products/{id}` | Public | Get product details by ID |
| **Categories**| `GET` | `/api/categories` | Public | List all active grocery departments |
| **Cart** | `GET` | `/api/customer/cart` | Customer | View customer shopping cart |
| **Cart** | `POST` | `/api/customer/cart/add` | Customer | Add/increment product in cart |
| **Cart** | `PUT` | `/api/customer/cart/update` | Customer | Update item quantity |
| **Cart** | `DELETE`| `/api/customer/cart/remove` | Customer | Remove item from cart |
| **Orders** | `POST` | `/api/customer/orders` | Customer | Checkout cart and place order |
| **Orders** | `GET` | `/api/customer/orders` | Customer | View customer order history |
| **Orders** | `GET` | `/api/customer/orders/{id}` | Customer | View itemized order details |
| **Orders** | `PATCH`| `/api/customer/orders/{id}/cancel` | Customer | Cancel order (with inventory rollback) |
| **Returns** | `POST` | `/api/customer/returns` | Customer | Request return for delivered order |
| **Exchanges**| `POST` | `/api/customer/exchanges` | Customer | Request item replacement |
| **Admin** | `GET` | `/api/admin/orders` | Admin | List all store customer orders |
| **Admin** | `PATCH`| `/api/admin/orders/{id}/status` | Admin | Update fulfillment status |
| **Admin** | `POST` | `/api/admin/products` | Admin | Create new grocery product |
| **Admin** | `PUT` | `/api/admin/products/{id}` | Admin | Update product details and stock |
| **Admin** | `PATCH`| `/api/admin/products/{id}/status` | Admin | Toggle product active status |
| **Admin** | `PATCH`| `/api/admin/returns/{id}/approve` | Admin | Approve return & restore inventory |
| **Admin** | `PATCH`| `/api/admin/exchanges/{id}/approve` | Admin | Approve exchange & reserve stock |
| **Admin** | `GET` | `/api/admin/audit-logs` | Admin | View chronological audit log trail |

---

## 📁 Project Structure

```
minidmart/
├── src/
│   ├── main/
│   │   ├── java/com/miniproject/minidmart/
│   │   │   ├── config/            # SecurityConfig, OpenApiConfig, CorsConfig
│   │   │   ├── controller/        # REST Controllers (Auth, Product, Cart, Order, Return, Exchange, Audit)
│   │   │   ├── dto/               # Data Transfer Objects (Requests & Responses)
│   │   │   ├── entity/            # JPA Entities (User, Product, Category, Cart, Order, Return, Exchange, AuditLog)
│   │   │   ├── enums/             # Role, OrderStatus, DeliveryType, RequestStatus, etc.
│   │   │   ├── exception/         # GlobalExceptionHandler & Custom Domain Exceptions
│   │   │   ├── repository/        # Spring Data JPA Repository Interfaces
│   │   │   ├── security/          # JwtService, JwtAuthenticationFilter, CustomUserDetailsService
│   │   │   ├── service/           # Business Logic Services (OrderService, ReturnService, ExchangeService, AuditService)
│   │   │   └── util/              # DataInitializer (Demo data bootstrap)
│   │   └── resources/
│   │       ├── static/            # Multi-Page Frontend (HTML, CSS, Vanilla JS)
│   │       │   ├── admin/         # Admin Management HTML Views
│   │       │   ├── css/           # Modular CSS Stylesheets (main, navbar, cards, tables, admin)
│   │       │   └── js/            # Modular ES6 JavaScript (api, auth, products, cart, orders, returns, exchanges, admin)
│   │       └── application.properties # Server port, PostgreSQL DataSource, JPA config
├── API_DOCUMENTATION.md           # Exhaustive REST API specification
├── ARCHITECTURE.md                # In-depth architectural patterns & diagrams
├── INTERVIEW_PREP.md              # Technical interview Q&A guide
├── SECURITY.md                    # Security architecture & RBAC policies
├── .env.example                   # Environment configuration template
└── pom.xml                        # Maven dependencies & build plugins
```

---

## 📚 Documentation Index

For detailed technical references, refer to the accompanying documentation files:
1. **[API Documentation](API_DOCUMENTATION.md)** — Complete catalog of endpoints with request/response payloads.
2. **[Architecture Guide](ARCHITECTURE.md)** — Layered design, transaction boundaries, and state machines.
3. **[Interview Preparation Guide](INTERVIEW_PREP.md)** — Questions, answers, and architectural explanations for interviews.
4. **[Security Policy](SECURITY.md)** — Security architecture, password hashing, and token validation.
