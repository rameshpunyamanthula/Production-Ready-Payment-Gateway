# System Architecture

## Overview
Payment Gateway system with microservices architecture, containerized deployment, and separated frontend applications.

---

## Architecture Diagram


┌─────────────────────────────────────────────────────────────────┐
│                | PAYMENT GATEWAY SYSTEM │                       |
└─────────────────────────────────────────────────────────────────┘
                ┌──────────────────────┐
                │   Merchant Backend   │
                │  (External System)   │
                └──────────┬───────────┘
                           │ API Calls
                           │ (with API Key/Secret)
                           ↓
┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│  Dashboard   │────→│   Spring Boot   │────→│ PostgreSQL   │
│  (Port 3000) │     │   Backend API   │     │ (Port 5432)  │
│   React      │     │   (Port 8000)   │     │   Database   │
└──────────────┘     └────────┬────────┘     └──────────────┘
                              ↑
                              │ Public APIs
                              │ (No Auth)
                              │
                     ┌────────┴────────┐
                     │  Checkout Page  │
                     │  (Port 3001)    │
                     │     React       │
                     └─────────────────┘
                              ↑
                              │
                       ┌──────┴──────┐
                       │  Customer   │
                       │   Browser   │
                       └─────────────┘



---

## Component Details

### 1. Frontend Applications

#### Dashboard (Port 3000)
- **Purpose:** Merchant portal for managing orders and viewing transactions
- **Technology:** React, Axios
- **Features:**
  - Login with test merchant credentials
  - Display API credentials
  - Create new orders via UI
  - View all transactions
  - Calculate real-time statistics
- **Authentication:** Session-based (email only for Deliverable 1)
- **API Integration:** Calls authenticated backend endpoints

#### Checkout Page (Port 3001)
- **Purpose:** Customer-facing payment interface
- **Technology:** React, Axios
- **Features:**
  - Display order details
  - Payment method selection (UPI/Card)
  - Payment form submission
  - Status polling (every 2 seconds)
  - Success/Failure states
- **Authentication:** None (uses public endpoints)
- **API Integration:** Calls public backend endpoints

---

### 2. Backend API (Port 8000)

#### Technology Stack
- **Framework:** Spring Boot (Java)
- **Database:** PostgreSQL with JPA/Hibernate
- **Architecture:** RESTful API with layered architecture

#### Layers

**Controllers:**
- `HealthController` - System health checks
- `OrderController` - Authenticated order management
- `PaymentsController` - Authenticated payment operations
- `PublicPaymentsController` - Public checkout endpoints
- `TestController` - Test merchant verification

**Services:**
- `OrderService` - Order creation and retrieval logic
- `PaymentService` - Payment processing and validation
- `AuthenticationService` - API key/secret authentication
- `ValidationService` - VPA, Card (Luhn), expiry validation

**Repositories:**
- `MerchantRepository` - Merchant data access
- `OrderRepository` - Order data access
- `PaymentRepository` - Payment data access

**Models:**
- `Merchant` - Merchant entity with credentials
- `Order` - Order entity with amount and status
- `Payment` - Payment entity with method-specific fields

---

### 3. Database (Port 5432)

#### PostgreSQL Database
- **Database Name:** `payment_gateway`
- **User:** `gateway_user`
- **Password:** `gateway_pass`

#### Schema:
merchants (UUID)
├── id (PK)
├── name
├── email (UNIQUE)
├── api_key (UNIQUE)
├── api_secret
├── webhook_url
├── is_active
├── created_at
└── updated_at

orders (VARCHAR)
├── id (PK) - format: order_XXXXXXXXXXXXXXXX
├── merchant_id (FK → merchants.id)
├── amount (integer, paise)
├── currency
├── receipt
├── notes (JSONB)
├── status
├── created_at
└── updated_at

payments (VARCHAR)
├── id (PK) - format: pay_XXXXXXXXXXXXXXXX
├── order_id (FK → orders.id)
├── merchant_id (FK → merchants.id)
├── amount (integer, paise)
├── currency
├── method (upi/card)
├── status (processing/success/failed)
├── vpa (for UPI)
├── card_network (for Card)
├── card_last4 (for Card)
├── error_code
├── error_description
├── created_at
└── updated_at


---

## Data Flow

### Flow 1: Merchant Creates Order

Merchant → Dashboard → Click "Create Order"

Dashboard → Backend API (POST /api/v1/orders)
Headers: X-Api-Key, X-Api-Secret

Backend → Validate credentials

Backend → Generate order ID (order_XXXXXXXXXXXXXXXX)

Backend → Save to database

Backend → Return order object

Dashboard → Display checkout link

Customer → Click link → Redirect to Checkout (Port 3001)



### Flow 2: Customer Pays via Checkout

Checkout Page → Load with order_id param

Checkout → Backend (GET /api/v1/orders/{id}/public)

Backend → Return order details (no auth)

Customer → Select payment method (UPI/Card)

Customer → Submit payment form

Checkout → Backend (POST /api/v1/payments/public)

Backend → Validate VPA/Card

Backend → Create payment (status: processing)

Backend → Simulate processing (1.5s delay)

Backend → Update status (success/failed)

Backend → Return payment_id

Checkout → Start polling (every 2s)

Checkout → Backend (GET /api/v1/payments/{id}/public)

Backend → Return current status

Checkout → Show success/failure state


### Flow 3: Merchant Views Transactions

Merchant → Dashboard → Click "Transactions"

Dashboard → Backend (GET /api/v1/payments)
Headers: X-Api-Key, X-Api-Secret

Backend → Validate credentials

Backend → Query payments for merchant

Backend → Return payment array

Dashboard → Display in table


---

## Security Architecture

### Authentication
- **API Key + API Secret:** Used for merchant authentication
- **Header-based:** Credentials sent in X-Api-Key and X-Api-Secret headers
- **Validation:** Backend verifies both key and secret match

### Data Protection
- **Card Numbers:** Only last 4 digits stored
- **CVV:** Never stored
- **Passwords:** Not implemented in Deliverable 1 (simple email-based login)
- **API Secrets:** Stored in database, not exposed in frontend

### Public vs Protected Endpoints
- **Protected:** Require authentication (orders, payments management)
- **Public:** No authentication (checkout flow only)
- **Validation:** Public endpoints validate order ownership

---

## Deployment Architecture

### Docker Containers


┌─────────────────────────────────────────────┐
│ Docker Compose Network │
│ │
│ ┌────────────┐ ┌────────────┐ │
│ │ dashboard │ │ checkout │ │
│ │ (nginx) │ │ (nginx) │ │
│ │ :3000→80 │ │ :3001→80 │ │
│ └─────┬──────┘ └─────┬──────┘ │
│ │ │ │
│ └────────┬───────┘ │
│ ↓ │
│ ┌───────────────┐ │
│ │ gateway_api │ │
│ │ (Spring Boot)│ │
│ │ :8000→8000 │ │
│ └───────┬───────┘ │
│ │ │
│ ↓ │
│ ┌───────────────┐ │
│ │ pg_gateway │ │
│ │ (PostgreSQL) │ │
│ │ :5432→5432 │ │
│ └───────────────┘ │
│ │
└─────────────────────────────────────────────┘


### Startup Sequence
1. **PostgreSQL** starts first
2. **Health check** verifies database ready
3. **API** starts (depends on PostgreSQL health)
4. **Frontend apps** start (depend on API)
5. **Database seeding** runs on API startup

---

## Technology Stack Summary

| Component | Technology | Port |
|-----------|-----------|------|
| Dashboard | React, Nginx | 3000 |
| Checkout | React, Nginx | 3001 |
| Backend API | Spring Boot, Java | 8000 |
| Database | PostgreSQL 15 | 5432 |
| Container | Docker, Docker Compose | - |

---

## Scalability Considerations

### Current Design
- Monolithic backend API
- Single PostgreSQL instance
- Synchronous payment processing

### Future Enhancements
- **Redis:** For caching and session management
- **Message Queue:** For asynchronous payment processing
- **Worker Service:** For background jobs
- **Load Balancer:** For multiple API instances
- **Database Replica:** For read scaling

