# API Reference

Base URL: `http://localhost:7321/lockdoc`

All paths below are relative to the base URL. Paths marked **Public** bypass JWT validation (see `app.security.jwt.excluded-urls` in `application.properties`); everything else requires:

```
Authorization: Bearer <token>
```

## Auth

### `POST /auth/login` — Public

Authenticate and receive a JWT.

**Request body**
```json
{
  "username": "FP_USER",
  "password": "FP@Admin#123"
}
```

**Response `200 OK`**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "FP_USER",
  "roles": ["SUPER_ADMIN"],
  "rights": ["USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE", "ROLE_MANAGE", "RIGHT_MANAGE", "SYSTEM_ADMIN"],
  "tokenType": "Bearer"
}
```

**Response `401 Unauthorized`** — invalid credentials
```json
{
  "timestamp": "2026-09-06T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password"
}
```

## Users

### `GET /users/me` — Protected

Returns the profile of the currently authenticated user (derived from the JWT).

**Response `200 OK`**
```json
{
  "username": "FP_USER",
  "email": "fp_user@lockdoc.local",
  "fullName": "FP Super Admin",
  "roles": ["SUPER_ADMIN"]
}
```

## Health

### `GET /actuator/health` — Public

Standard Spring Boot Actuator health payload.

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

## H2 Console (development only) — Public

```
GET /h2-console
```

- JDBC URL: `jdbc:h2:file:./data/lockdocdb`
- Username: `sa`
- Password: *(blank)*

## Error format

All handled errors follow this shape (see `GlobalExceptionHandler`):

```json
{
  "timestamp": "2026-09-06T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "username: Username is required"
}
```
