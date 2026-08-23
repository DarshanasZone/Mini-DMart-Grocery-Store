# 📖 Mini-DMart REST API Reference Manual

Complete technical specification of all RESTful endpoints implemented in the Mini-DMart application.

---

## 🌐 Base URL & Common Headers

- **Base URL**: `http://localhost:8090/api`
- **Content-Type**: `application/json`
- **Authentication**: `Authorization: Bearer <jwt_token>` (for protected endpoints)

---

## 1. Authentication Endpoints

### 1.1 Register Customer
Registers a new customer account and creates an empty shopping cart.

- **Endpoint**: `POST /auth/register`
- **Access**: Public
- **Request Body**:
```json
{
  "name": "Darshana Sharma",
  "email": "darshana@example.com",
  "password": "Password@123"
}
```
- **Response `200 OK`**:
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "userId": 7,
  "name": "Darshana Sharma",
  "email": "darshana@example.com",
  "role": "CUSTOMER"
}
```

### 1.2 User Login
Authenticates user credentials and returns a signed JWT token.

- **Endpoint**: `POST /auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "email": "customer@dmart.com",
  "password": "Customer@123"
}
```
- **Response `200 OK`**:
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "userId": 6,
  "name": "Darshana Customer",
  "email": "customer@dmart.com",
  "role": "CUSTOMER"
}
```

---

## 2. Product & Category Endpoints

### 2.1 List All Active Products
- **Endpoint**: `GET /customer/products`
- **Access**: Public
- **Query Params**:
  - `q` *(optional)*: Search keyword
  - `categoryId` *(optional)*: Filter by department category
- **Response `200 OK`**:
```json
[
  {
    "id": 8,
    "name": "Britannia Processed Cheese Slices 200g",
    "description": "Classic rich melted cheese slices for sandwiches.",
    "price": 140.00,
    "stockQuantity": 45,
    "imageUrl": "https://images.unsplash.com/photo-1624806992066-5ffcf7ca186b?w=600",
    "active": true,
    "category": {
      "id": 3,
      "name": "Dairy & Eggs",
      "description": "Fresh milk, cheese, butter, yogurt, and farm fresh eggs.",
      "active": true
    }
  }
]
```

### 2.2 Get Product by ID
- **Endpoint**: `GET /customer/products/{id}`
- **Access**: Public
- **Response `200 OK`**: Single product JSON object.

### 2.3 List Categories
- **Endpoint**: `GET /categories`
- **Access**: Public
- **Response `200 OK`**: Array of category objects.

---

## 3. Shopping Cart Endpoints

### 3.1 View Customer Cart
- **Endpoint**: `GET /customer/cart`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Response `200 OK`**:
```json
{
  "id": 2,
  "items": [
    {
      "id": 5,
      "product": {
        "id": 8,
        "name": "Britannia Processed Cheese Slices 200g",
        "price": 140.00,
        "stockQuantity": 45,
        "imageUrl": "https://images.unsplash.com/photo-1624806992066-5ffcf7ca186b?w=600"
      },
      "quantity": 2
    }
  ]
}
```

### 3.2 Add / Increment Cart Item
- **Endpoint**: `POST /customer/cart/add?productId={id}&quantity={qty}`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Response `200 OK`**: Updated Cart object.

### 3.3 Update Item Quantity
- **Endpoint**: `PUT /customer/cart/update?productId={id}&quantity={qty}`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Response `200 OK`**: Updated Cart object.

### 3.4 Remove Item from Cart
- **Endpoint**: `DELETE /customer/cart/remove?productId={id}`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Response `200 OK`**: Updated Cart object.

---

## 4. Customer Orders & Post-Purchase Endpoints

### 4.1 Place Order (Checkout)
- **Endpoint**: `POST /customer/orders`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Request Body**:
```json
{
  "deliveryType": "HOME_DELIVERY",
  "deliveryAddress": "Flat 402, Palm Heights, Mumbai - 400001",
  "pickupDate": null
}
```
- **Response `200 OK`**:
```json
{
  "id": 4,
  "userEmail": "customer@dmart.com",
  "userName": "Darshana Customer",
  "totalAmount": 280.00,
  "status": "PLACED",
  "deliveryType": "HOME_DELIVERY",
  "deliveryAddress": "Flat 402, Palm Heights, Mumbai - 400001",
  "pickupDate": null,
  "createdAt": "2026-08-23T11:12:51.123",
  "canCancel": true,
  "canReturnOrExchange": false,
  "items": [
    {
      "id": 8,
      "productId": 8,
      "productName": "Britannia Processed Cheese Slices 200g",
      "productImageUrl": "https://images.unsplash.com/photo-1624806992066-5ffcf7ca186b?w=600",
      "quantity": 2,
      "price": 140.00,
      "subtotal": 280.00
    }
  ]
}
```

### 4.2 Customer Order History
- **Endpoint**: `GET /customer/orders`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Response `200 OK`**: Array of `OrderResponse` objects.

### 4.3 Cancel Order
- **Endpoint**: `PATCH /customer/orders/{id}/cancel`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Response `200 OK`**: Updated `OrderResponse` with status `CANCELLED`.

### 4.4 Submit Return Request
- **Endpoint**: `POST /customer/returns`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Request Body**:
```json
{
  "orderId": 4,
  "reason": "Damaged outer packaging received"
}
```
- **Response `200 OK`**:
```json
{
  "id": 3,
  "orderId": 4,
  "userEmail": "customer@dmart.com",
  "reason": "Damaged outer packaging received",
  "status": "REQUESTED",
  "createdAt": "2026-08-23T11:12:52.456",
  "processedAt": null
}
```

### 4.5 Submit Exchange Request
- **Endpoint**: `POST /customer/exchanges`
- **Access**: Customer (`ROLE_CUSTOMER`)
- **Request Body**:
```json
{
  "orderId": 4,
  "oldProductId": 8,
  "newProductId": 9,
  "reason": "Prefer fresh apples instead of cheese"
}
```
- **Response `200 OK`**:
```json
{
  "id": 1,
  "orderId": 4,
  "userEmail": "customer@dmart.com",
  "oldProductId": 8,
  "oldProductName": "Britannia Processed Cheese Slices 200g",
  "newProductId": 9,
  "newProductName": "Fresh Shimla Apples 1kg",
  "reason": "Prefer fresh apples instead of cheese",
  "status": "REQUESTED",
  "createdAt": "2026-08-23T11:12:58.789",
  "processedAt": null
}
```

---

## 5. Admin Management Endpoints

### 5.1 List All Orders
- **Endpoint**: `GET /admin/orders`
- **Access**: Admin (`ROLE_ADMIN`)
- **Response `200 OK`**: Array of all customer orders.

### 5.2 Update Order Status
- **Endpoint**: `PATCH /admin/orders/{id}/status`
- **Access**: Admin (`ROLE_ADMIN`)
- **Request Body**:
```json
{
  "status": "DELIVERED"
}
```
- **Response `200 OK`**: Updated `OrderResponse`.

### 5.3 Manage Products (CRUD)
- **Create**: `POST /admin/products`
- **Update**: `PUT /admin/products/{id}`
- **Toggle Active**: `PATCH /admin/products/{id}/status`
- **Access**: Admin (`ROLE_ADMIN`)

### 5.4 Manage Categories (CRUD)
- **Create**: `POST /admin/categories`
- **Update**: `PUT /admin/categories/{id}`
- **Delete**: `DELETE /admin/categories/{id}`
- **Access**: Admin (`ROLE_ADMIN`)

### 5.5 Process Returns & Exchanges
- **Approve Return**: `PATCH /admin/returns/{id}/approve` (Restocks items)
- **Reject Return**: `PATCH /admin/returns/{id}/reject`
- **Approve Exchange**: `PATCH /admin/exchanges/{id}/approve` (Reserves new item & restocks old item)
- **Reject Exchange**: `PATCH /admin/exchanges/{id}/reject`

### 5.6 View Audit Logs
- **Endpoint**: `GET /admin/audit-logs`
- **Access**: Admin (`ROLE_ADMIN`)
- **Response `200 OK`**: Chronological list of administrative actions.
