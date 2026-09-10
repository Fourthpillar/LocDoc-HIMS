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

## Pharmacy module

All pharmacy endpoints are **Protected** and additionally enforce a fine-grained right via `@PreAuthorize("hasAuthority('RIGHT_CODE')")` (see `CLAUDE.md` for the convention). The default `FP_USER` / `SUPER_ADMIN` account has every right below; a new `PHARMACIST` role also has all of them.

List endpoints return the shared paginated shape:
```json
{ "content": [ ... ], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
```
and accept `?page=&size=&search=` (search is optional, case-insensitive, matches name/code).

### Medicines - `/pharmacy/medicines` (right: `PHARMACY_MEDICINE_MANAGE`, read also allowed with `PHARMACY_INVENTORY_READ`)

- `GET /pharmacy/medicines?page=&size=&search=`
- `GET /pharmacy/medicines/{id}`
- `POST /pharmacy/medicines` — body: `{code, name, genericName, manufacturer, category, uom, hsnCode, taxPercent, reorderLevel, isScheduleDrug}`
- `PUT /pharmacy/medicines/{id}`
- `PATCH /pharmacy/medicines/{id}/deactivate` — soft delete (`active=false`)

### Suppliers - `/pharmacy/suppliers` (right: `PHARMACY_SUPPLIER_MANAGE`)

- `GET /pharmacy/suppliers?page=&size=&search=`
- `GET /pharmacy/suppliers/{id}`
- `POST /pharmacy/suppliers` — body: `{name, contactPerson, phone, email, address, gstin}`
- `PUT /pharmacy/suppliers/{id}`
- `PATCH /pharmacy/suppliers/{id}/deactivate`

### Patients - `/outpatient/patients` (right: `OUTPATIENT_PATIENT_MANAGE`)

Lives in the `outpatient` module, not `pharmacy` - `SalesService` (pharmacy) reads patient
data only through outpatient's `PatientService` bean, never its entity/repository directly.

- `GET /outpatient/patients?page=&size=&search=`
- `GET /outpatient/patients/{id}`
- `POST /outpatient/patients` — body: `{fullName, phone, gender, dateOfBirth, address}`. `mrn` is server-generated (`PT-{year}-{seq}`).
- `PUT /outpatient/patients/{id}`
- `PATCH /outpatient/patients/{id}/deactivate`

### Inventory - `/pharmacy/inventory` (right: `PHARMACY_INVENTORY_READ`)

- `GET /pharmacy/inventory?page=&size=&lowStockOnly=` — per-medicine stock summary (non-expired batches only)
- `GET /pharmacy/inventory/{medicineId}/batches` — active batches (`quantityOnHand > 0`) for a medicine, FEFO order (earliest expiry first)

### Purchase Orders - `/pharmacy/purchase-orders` (right: `PHARMACY_PURCHASE_ORDER_CREATE` for read/create, `PHARMACY_PURCHASE_ORDER_APPROVE` for approve/cancel)

- `GET /pharmacy/purchase-orders?page=&size=&search=`
- `GET /pharmacy/purchase-orders/{id}`
- `POST /pharmacy/purchase-orders` — body: `{supplierId, orderDate, expectedDeliveryDate, remarks, items:[{medicineId, orderedQty, rate, taxPercent}]}`. Creates in `DRAFT`, generates `po_number` (`PO-{year}-{seq}`).
- `POST /pharmacy/purchase-orders/{id}/approve` — `DRAFT` → `APPROVED` only
- `POST /pharmacy/purchase-orders/{id}/cancel` — `DRAFT`/`APPROVED` → `CANCELLED`, only if no purchase has been received against it

### Purchases (GRN) - `/pharmacy/purchases` (right: `PHARMACY_PURCHASE_CREATE`)

- `GET /pharmacy/purchases?page=&size=&search=`
- `GET /pharmacy/purchases/{id}`
- `POST /pharmacy/purchases` — body: `{purchaseOrderId?, supplierId, purchaseDate, supplierInvoiceNumber?, supplierInvoiceDate?, remarks, items:[{medicineId, purchaseOrderItemId?, batchNo, expiryDate, receivedQty, freeQty, rate, taxPercent, mrp, saleRate}]}`. Generates `grn_number` (`GRN-{year}-{seq}`); upserts the medicine batch, writes a `PURCHASE` stock ledger entry, and (if linked to a PO) advances that PO item's `received_qty` and recomputes the PO's status (`RECEIVED`/`PARTIALLY_RECEIVED`).
- `POST /pharmacy/purchases/{id}/cancel` — reverses the batch/ledger impact; rejected if any affected batch's stock has since dropped below what this purchase added

### Sales - `/pharmacy/sales` (right: `PHARMACY_SALE_CREATE`)

- `GET /pharmacy/sales?page=&size=&search=`
- `GET /pharmacy/sales/{id}`
- `POST /pharmacy/sales` — body: `{patientId?, walkInCustomerName?, walkInCustomerPhone?, saleDate, paymentMode, amountPaid, items:[{medicineId, medicineBatchId, qty, rate, taxPercent, discountAmount}]}`. Generates `invoice_number` (`INV-{year}-{seq}`); decrements the named batch and writes a `SALE` stock ledger entry. `409 InsufficientStockException` if the batch doesn't have enough quantity.
- `POST /pharmacy/sales/{id}/cancel` — reverses stock; rejected if any sales return already exists against the invoice

### Sales Returns - `/pharmacy/sales-returns` (right: `PHARMACY_SALE_RETURN_CREATE`)

- `GET /pharmacy/sales-returns?page=&size=&search=`
- `GET /pharmacy/sales-returns/{id}`
- `POST /pharmacy/sales-returns` — body: `{salesInvoiceId, returnDate, reason, items:[{salesInvoiceItemId, qty}]}`. Generates `return_number` (`SR-{year}-{seq}`); qty is capped at what remains un-returned on that invoice line; increments the batch and writes a `SALES_RETURN` stock ledger entry.
- `POST /pharmacy/sales-returns/{id}/cancel` — reverses stock back down; `409 InsufficientStockException` if that would go negative

### Reports - `/pharmacy/reports` (right: `PHARMACY_REPORT_READ`)

- `GET /pharmacy/reports/stock-summary?page=&size=`
- `GET /pharmacy/reports/expiry?withinDays=30`
- `GET /pharmacy/reports/low-stock`
- `GET /pharmacy/reports/sales-register?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /pharmacy/reports/purchase-register?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /pharmacy/reports/sales-return-register?from=YYYY-MM-DD&to=YYYY-MM-DD`

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
