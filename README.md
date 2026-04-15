# GalleryMart

Backend REST API cho do an nho ban tranh (Spring Boot 3 + JWT + JPA).

## 1. Project Scope

GalleryMart backend cung cap cac chuc nang MVP:
- Auth JWT: register, login, profile.
- Artwork: CRUD cho seller + search public co pagination.
- Order flow: buyer tao order, danh dau payment sent, seller confirm/cancel.
- Notification: list + mark all read; tao notification khi order doi trang thai.
- Scheduler: auto-cancel order PENDING het han va tra artwork ve AVAILABLE.

## 2. Tech Stack

- Java 17
- Spring Boot 3.2.4
- Spring Security (stateless JWT)
- Spring Data JPA + Hibernate
- MySQL (runtime)
- H2 (test)
- JUnit 5 + Mockito + MockMvc

## 3. Quick Start

### 3.1 Prerequisites

- JDK 17
- Maven 3.9+
- MySQL 8+

### 3.2 Clone va chay backend

```bash
git clone <your-repo-url>
cd GalleryMart/backend
mvn spring-boot:run
```

Mac dinh server chay o: `http://localhost:8080`

## 4. Configuration

File chinh: [backend/src/main/resources/application.properties](backend/src/main/resources/application.properties)

### 4.1 Database

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/art_marketplace?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 4.2 JWT

Khuyen nghi set env var trong production:

```bash
JWT_SECRET=<base64-or-strong-secret>
```

Default trong local da co fallback key de demo.

### 4.3 Cloudinary

Neu chua dung upload media, co the de gia tri mac dinh.

```bash
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

### 4.4 Scheduler

Tan suat job auto-cancel order het han:

```properties
order.expiration-check-interval-ms=60000
```

## 5. API Response Format

Tat ca endpoint tra ve wrapper:

```json
{
	"success": true,
	"message": "optional",
	"data": {}
}
```

Validation error tra ve `success=false`, `message="Validation failed"` va field errors trong `data`.

## 6. Authentication

Dung header cho endpoint protected:

```http
Authorization: Bearer <access_token>
```

## 7. API Endpoints

### 7.1 Auth

- `POST /api/auth/register` (public)
- `POST /api/auth/login` (public)
- `GET /api/auth/me` (authenticated)

### 7.2 Artworks

- `GET /api/artworks` (public search)
- `GET /api/artworks/{id}` (public)
- `GET /api/artworks/my` (SELLER)
- `POST /api/artworks` (SELLER)
- `PUT /api/artworks/{id}` (SELLER + owner)
- `DELETE /api/artworks/{id}` (SELLER + owner, chi AVAILABLE)

Search params:
- `category` (optional)
- `minPrice` (optional)
- `maxPrice` (optional)
- `keyword` (optional)
- `page` (default `0`)
- `size` (default `12`, max `100`)

### 7.3 Orders

- `POST /api/orders` (BUYER)
- `PATCH /api/orders/{id}/payment-sent` (BUYER)
- `PATCH /api/orders/{id}/confirm` (SELLER)
- `PATCH /api/orders/{id}/cancel` (SELLER)
- `GET /api/orders/my` (BUYER)
- `GET /api/orders/sales` (SELLER)
- `GET /api/orders/{id}` (buyer/seller lien quan)

Business rules chinh:
- Buyer khong duoc mua tranh cua chinh minh.
- Khong duoc dat mua tranh da SOLD.
- Tao order se dat artwork sang RESERVED.
- Confirm order se dat artwork sang SOLD.
- Cancel order se dat artwork ve AVAILABLE.

### 7.4 Notifications

- `GET /api/notifications` (authenticated)
- `PATCH /api/notifications/read-all` (authenticated)

## 8. Test Guide

### 8.1 Chay toan bo test

```bash
cd backend
mvn test
```

### 8.2 Integration tests (MockMvc + H2)

- [backend/src/test/java/com/gallerymart/backend/integration/AuthIntegrationTest.java](backend/src/test/java/com/gallerymart/backend/integration/AuthIntegrationTest.java)
- [backend/src/test/java/com/gallerymart/backend/integration/ArtworkIntegrationTest.java](backend/src/test/java/com/gallerymart/backend/integration/ArtworkIntegrationTest.java)
- [backend/src/test/java/com/gallerymart/backend/integration/OrderIntegrationTest.java](backend/src/test/java/com/gallerymart/backend/integration/OrderIntegrationTest.java)

### 8.3 Unit tests (Mockito)

- [backend/src/test/java/com/gallerymart/backend/unit/AuthServiceUnitTest.java](backend/src/test/java/com/gallerymart/backend/unit/AuthServiceUnitTest.java)
- [backend/src/test/java/com/gallerymart/backend/unit/ArtworkServiceImplUnitTest.java](backend/src/test/java/com/gallerymart/backend/unit/ArtworkServiceImplUnitTest.java)
- [backend/src/test/java/com/gallerymart/backend/unit/OrderServiceImplUnitTest.java](backend/src/test/java/com/gallerymart/backend/unit/OrderServiceImplUnitTest.java)

## 9. Current Limitations (MVP)

- Chua co refresh token.
- Chua co payment gateway that su (moi co luong payment sent marker).
- Chua co upload image flow day du su dung Cloudinary.
- Chua co OpenAPI/Swagger docs.

## 10. Suggested Next Steps

- Them refresh token + logout.
- Them OpenAPI (springdoc) cho API docs.
- Them rate limit cho auth endpoints.
- Them CI pipeline chay `mvn test` tren pull request.
