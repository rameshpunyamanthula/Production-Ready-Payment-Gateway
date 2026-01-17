# Payment Gateway API Documentation

## Base URL
http://localhost:8000



## Authentication

Protected endpoints require these headers:
X-Api-Key: key_test_abc123
X-Api-Secret: secret_test_xyz789



---

## Endpoints

### 1. Health Check
Check system health and database connectivity.

**Endpoint:** `GET /health`

**Authentication:** Not required

**Response 200:**
```json
{
  "status": "healthy",
  "database": "connected",
  "timestamp": "2026-01-10T07:30:00Z"
}
2. Test Merchant
Verify test merchant is seeded correctly.

Endpoint: GET /api/v1/test/merchant

Authentication: Not required

Response 200:

json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "test@example.com",
  "api_key": "key_test_abc123",
  "seeded": true
}
Response 404:

json
{
  "error": "Test merchant not found"
}
3. Create Order
Create a new payment order.

Endpoint: POST /api/v1/orders

Authentication: Required

Headers:


X-Api-Key: key_test_abc123
X-Api-Secret: secret_test_xyz789
Content-Type: application/json
Request Body:

json
{
  "amount": 50000,
  "currency": "INR",
  "receipt": "receipt_123",
  "notes": {
    "customer_name": "John Doe"
  }
}
Response 201:

json
{
  "id": "order_NXhj67fGH2jk9mPq",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50000,
  "currency": "INR",
  "receipt": "receipt_123",
  "notes": {
    "customer_name": "John Doe"
  },
  "status": "created",
  "created_at": "2026-01-10T07:30:00Z"
}
Error 401:

json
{
  "error": {
    "code": "AUTHENTICATION_ERROR",
    "description": "Invalid API credentials"
  }
}
Error 400:

json
{
  "error": {
    "code": "BAD_REQUEST_ERROR",
    "description": "amount must be at least 100"
  }
}
4. Get Order
Retrieve order details by ID.

Endpoint: GET /api/v1/orders/{order_id}

Authentication: Required

Headers:

X-Api-Key: key_test_abc123
X-Api-Secret: secret_test_xyz789
Response 200:

json
{
  "id": "order_NXhj67fGH2jk9mPq",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50000,
  "currency": "INR",
  "receipt": "receipt_123",
  "notes": {},
  "status": "created",
  "created_at": "2026-01-10T07:30:00Z",
  "updated_at": "2026-01-10T07:30:00Z"
}
Error 404:

json
{
  "error": {
    "code": "NOT_FOUND_ERROR",
    "description": "Order not found"
  }
}
5. Get Order (Public)
Retrieve order details without authentication (for checkout page).

Endpoint: GET /api/v1/orders/{order_id}/public

Authentication: Not required

Response 200:

json
{
  "id": "order_NXhj67fGH2jk9mPq",
  "amount": 50000,
  "currency": "INR",
  "status": "created"
}
6. Create Payment
Create a new payment for an order.

Endpoint: POST /api/v1/payments

Authentication: Required

Headers:

X-Api-Key: key_test_abc123
X-Api-Secret: secret_test_xyz789
Content-Type: application/json
Request Body (UPI):

json
{
  "order_id": "order_NXhj67fGH2jk9mPq",
  "method": "upi",
  "vpa": "user@paytm"
}
Request Body (Card):

json
{
  "order_id": "order_NXhj67fGH2jk9mPq",
  "method": "card",
  "card": {
    "number": "4111111111111111",
    "expiry_month": "12",
    "expiry_year": "2025",
    "cvv": "123",
    "holder_name": "John Doe"
  }
}
Response 201 (UPI):

json
{
  "id": "pay_H8sK3jD9s2L1pQr",
  "order_id": "order_NXhj67fGH2jk9mPq",
  "amount": 50000,
  "currency": "INR",
  "method": "upi",
  "vpa": "user@paytm",
  "status": "processing",
  "created_at": "2026-01-10T07:31:00Z"
}
Response 201 (Card):

json
{
  "id": "pay_H8sK3jD9s2L1pQr",
  "order_id": "order_NXhj67fGH2jk9mPq",
  "amount": 50000,
  "currency": "INR",
  "method": "card",
  "card_network": "visa",
  "card_last4": "1111",
  "status": "processing",
  "created_at": "2026-01-10T07:31:00Z"
}
7. Create Payment (Public)
Create payment without authentication (for checkout page).

Endpoint: POST /api/v1/payments/public

Authentication: Not required

Headers:


Content-Type: application/json
Request Body: Same as Create Payment

Response 201:

json
{
  "payment_id": "pay_H8sK3jD9s2L1pQr",
  "status": "processing"
}
8. Get Payment
Retrieve payment details by ID.

Endpoint: GET /api/v1/payments/{payment_id}

Authentication: Required

Headers:

X-Api-Key: key_test_abc123
X-Api-Secret: secret_test_xyz789
Response 200:

json
{
  "id": "pay_H8sK3jD9s2L1pQr",
  "order_id": "order_NXhj67fGH2jk9mPq",
  "amount": 50000,
  "currency": "INR",
  "method": "upi",
  "vpa": "user@paytm",
  "status": "success",
  "created_at": "2026-01-10T07:31:00Z",
  "updated_at": "2026-01-10T07:31:10Z"
}
9. Get Payment (Public)
Retrieve payment status without authentication (for checkout page polling).

Endpoint: GET /api/v1/payments/{payment_id}/public

Authentication: Not required

Response 200:

json
{
  "payment_id": "pay_H8sK3jD9s2L1pQr",
  "order_id": "order_NXhj67fGH2jk9mPq",
  "amount": 50000,
  "currency": "INR",
  "method": "upi",
  "status": "success"
}
10. Get All Payments
Retrieve all payments for authenticated merchant.

Endpoint: GET /api/v1/payments

Authentication: Required

Headers:


X-Api-Key: key_test_abc123
X-Api-Secret: secret_test_xyz789
Response 200:

json
[
  {
    "id": "pay_H8sK3jD9s2L1pQr",
    "order_id": "order_NXhj67fGH2jk9mPq",
    "amount": 50000,
    "currency": "INR",
    "method": "upi",
    "vpa": "user@paytm",
    "status": "success",
    "created_at": "2026-01-10T07:31:00Z",
    "updated_at": "2026-01-10T07:31:10Z"
  }
]
Payment Validation
VPA Validation
Pattern: ^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$

Valid: user@paytm, john.doe@bank, user_123@phonepe

Invalid: user @bank, @bank, user@@bank

Card Validation
Luhn Algorithm: Validates card number checksum

Network Detection:

Visa: starts with 4

Mastercard: starts with 51-55

Amex: starts with 34 or 37

RuPay: starts with 60, 65, or 81-89

Expiry: Must be future date (accepts MM/YY or MM/YYYY)

Security: Only last 4 digits stored, CVV never stored

Payment Flow
Create Order (merchant backend)

Redirect to Checkout with order_id

Fetch Order Details (public endpoint)

Submit Payment (public endpoint) → Returns payment_id

Poll Payment Status every 2 seconds (public endpoint)

Show Success/Failure based on status

Error Codes
Code	Description
AUTHENTICATION_ERROR	Invalid API credentials
BAD_REQUEST_ERROR	Validation errors
NOT_FOUND_ERROR	Resource not found
INVALID_VPA	VPA format invalid
INVALID_CARD	Card validation failed
EXPIRED_CARD	Card expiry invalid
PAYMENT_FAILED	Payment processing failed
text
