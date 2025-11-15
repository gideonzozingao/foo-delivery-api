
This outline is production-ready and will guide you from **project structure → database design → modules → controllers → services → security → DTOs → migrations → utilities → testing**.

---

# ✅ **1. Project Architecture Overview**

We will follow **Clean Architecture + Layered Structure**:

```
controller → service → repository → entity
```

Plus additional layers for:

* **DTOs**
* **Mappers**
* **Security (JWT)**
* **Config**
* **Exception handling**
* **Flyway migrations**
* **Utility helpers**

---

# ✅ **2.  Project Structure**

```
src/
 └── main/
     ├── java/com/zuqongtch/fooddelivery/
     │    ├── FooDeliveryAppApplication.java
     │    │
     │    ├── config/
     │    │    ├── SecurityConfig.java
     │    │    ├── JwtAuthFilter.java
     │    │    ├── OpenApiConfig.java
     │    │    └── PasswordEncoderConfig.java
     │    │
     │    ├── exception/
     │    │    ├── GlobalExceptionHandler.java
     │    │    ├── ResourceNotFoundException.java
     │    │    ├── BadRequestException.java
     │    │    └── UnauthorizedException.java
     │    │
     │    ├── security/
     │    │    ├── JwtUtil.java
     │    │    ├── CustomUserDetails.java
     │    │    ├── CustomUserDetailsService.java
     │    │    └── SecurityConstants.java
     │    │
     │    ├── entity/
     │    │    ├── User.java
     │    │    ├── Role.java
     │    │    ├── MenuItem.java
     │    │    ├── Category.java
     │    │    ├── Cart.java
     │    │    ├── CartItem.java
     │    │    ├── Order.java
     │    │    ├── OrderItem.java
     │    │    ├── Address.java
     │    │    └── Payment.java
     │    │
     │    ├── repository/
     │    │    ├── UserRepository.java
     │    │    ├── RoleRepository.java
     │    │    ├── MenuItemRepository.java
     │    │    ├── CategoryRepository.java
     │    │    ├── OrderRepository.java
     │    │    └── CartRepository.java
     │    │
     │    ├── dto/
     │    │    ├── auth/
     │    │    │    ├── RegisterRequest.java
     │    │    │    ├── LoginRequest.java
     │    │    │    └── AuthResponse.java
     │    │    ├── menu/
     │    │    │    ├── MenuItemRequest.java
     │    │    │    └── MenuItemResponse.java
     │    │    ├── order/
     │    │    │    ├── OrderRequest.java
     │    │    │    └── OrderResponse.java
     │    │    └── cart/
     │    │         ├── AddToCartRequest.java
     │    │         └── CartResponse.java
     │    │
     │    ├── mapper/
     │    │    ├── MenuItemMapper.java
     │    │    └── OrderMapper.java
     │    │
     │    ├── service/
     │    │    ├── AuthService.java
     │    │    ├── MenuService.java
     │    │    ├── OrderService.java
     │    │    ├── CartService.java
     │    │    └── UserService.java
     │    │
     │    ├── controller/
     │    │    ├── AuthController.java
     │    │    ├── MenuController.java
     │    │    ├── CartController.java
     │    │    └── OrderController.java
     │    │
     │    ├── util/
     │    │    ├── DateUtil.java
     │    │    └── Helpers.java
     │    │
     │    └── constant/
     │         ├── OrderStatus.java
     │         ├── PaymentStatus.java
     │         └── Roles.java
     │
     └── resources/
          ├── application.yml
          ├── static/
          ├── templates/
          └── db/migration/
                ├── V1__init_schema.sql
                ├── V2__insert_roles.sql
                ├── V3__sample_menu_items.sql
```

---

# ✅ **3. Database Schema (ERD)**

### Entities:

* **User**
* **Role**
* **MenuItem**
* **Category**
* **Cart + CartItem**
* **Order + OrderItem**
* **Address**
* **Payment**

### Relationships:

* User → Orders (`1..n`)
* User → Cart (`1..1`)
* Order → OrderItem (`1..n`)
* MenuItem → Category (`n..1`)
* Cart → CartItem (`1..n`)

---

# ✅ **4. Major Modules & Endpoints**

---

## **🔐 Authentication Module**

### Entities:

* User
* Role

### Endpoints:

| Method | URI                  | Description     |
| ------ | -------------------- | --------------- |
| POST   | `/api/auth/register` | Register user   |
| POST   | `/api/auth/login`    | Login & get JWT |

### Features:

* Hash password using BCrypt
* JWT token with roles
* Role-based authorization (ADMIN, CUSTOMER)

---

## **🍽 Menu Module**

### Endpoints:

| Method | URI              | Description           |
| ------ | ---------------- | --------------------- |
| GET    | `/api/menu`      | List all menu items   |
| GET    | `/api/menu/{id}` | Get menu item         |
| POST   | `/api/menu`      | Add menu item (Admin) |
| PUT    | `/api/menu/{id}` | Update item           |
| DELETE | `/api/menu/{id}` | Delete                |

---

## **🛒 Cart Module**

### Endpoints:

| Method | URI                  | Description      |
| ------ | -------------------- | ---------------- |
| POST   | `/api/cart/add`      | Add item to cart |
| GET    | `/api/cart`          | View cart        |
| DELETE | `/api/cart/{itemId}` | Remove item      |

---

## **📦 Order Module**

### Endpoints:

| Method | URI                       | Description          |
| ------ | ------------------------- | -------------------- |
| POST   | `/api/orders`             | Place order          |
| GET    | `/api/orders`             | List user orders     |
| GET    | `/api/orders/{id}`        | View order           |
| PUT    | `/api/orders/{id}/status` | Admin: update status |

### Order statuses:

```
PENDING → PROCESSING → OUT_FOR_DELIVERY → COMPLETED → CANCELLED
```

---

# ✅ **5. Security Layer (JWT)**

You already included:

```
spring-boot-starter-security
```

Implement:

### **SecurityConfig**

* Disable CSRF
* Configure auth paths
* Add JwtAuthFilter
* Configure AuthenticationManager

### **JwtUtil**

* generateToken()
* validateToken()
* extractUsername()

### **UserDetailsService**

* loadUserByUsername()

---

# ✅ **6. Flyway Database Migrations**

Under:

```
src/main/resources/db/migration/
```

### V1__init_schema.sql

Create all tables.

### V2__insert_roles.sql

Insert ADMIN, CUSTOMER.

### V3__sample_menu_items.sql

Insert menu categories + mock food items.

---

# ✅ **7. DTO Layer (Never Expose Entities)**

Examples:

### MenuItemRequest

```java
public record MenuItemRequest(
    String name,
    String description,
    Double price,
    Long categoryId
) {}
```

### MenuItemResponse

```java
public record MenuItemResponse(
    Long id,
    String name,
    String description,
    Double price,
    String category
) {}
```

---

# ✅ **8. Services Logic**

Each service:

* Validates input
* Uses repositories
* Converts entities ↔ DTOs using mappers
* Throws custom exceptions

---

# ✅ **9. Global Exception Handling**

Create:

### GlobalExceptionHandler.java

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
```

---

# ✅ **10. Testing Strategy**

### Unit Tests (JUnit + Mockito)

* Services
* Utils
* JWT components

### Integration Tests

* Auth controller
* Menu controller
* Order controller

---

# ✅ **11. Final Deliverables Checklist**

✔ Complete project structure
✔ DTO + Entity design
✔ Proper services + controllers
✔ Flyway-enabled schema migration
✔ Global exception handling
✔ JWT security
✔ Sample data
✔ Role-based authorization

---# foo-delivery-api
