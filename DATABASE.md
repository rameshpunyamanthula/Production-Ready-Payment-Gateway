# Database Schema Documentation

## Overview
PostgreSQL database with three main tables: merchants, orders, and payments. All tables include proper relationships, indexes, and constraints.

---

## Tables

### 1. Merchants Table

Stores merchant account information and API credentials.

**Table Name:** `merchants`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique merchant identifier |
| `name` | VARCHAR(255) | NOT NULL | Merchant business name |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Merchant email address |
| `api_key` | VARCHAR(64) | NOT NULL, UNIQUE | API authentication key |
| `api_secret` | VARCHAR(64) | NOT NULL | API authentication secret |
| `webhook_url` | TEXT | NULL | URL for webhook notifications |
| `is_active` | BOOLEAN | DEFAULT TRUE | Account active status |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation time |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update time |

**Indexes:**
- Primary Key on `id`
- Unique index on `email`
- Unique index on `api_key`

**Sample Data:**
```sql
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Test Merchant",
  "email": "test@example.com",
  "api_key": "key_test_abc123",
  "api_secret": "secret_test_xyz789",
  "webhook_url": null,
  "is_active": true,
  "created_at": "2026-01-10T07:00:00Z",
  "updated_at": "2026-01-10T07:00:00Z"
}

2. Orders Table
Stores payment orders created by merchants.

Table Name: orders

| Column      | Type         | Constraints                     | Description                                       |
| ----------- | ------------ | ------------------------------- | ------------------------------------------------- |
| id          | VARCHAR(64)  | PRIMARY KEY                     | Order identifier (format: order_XXXXXXXXXXXXXXXX) |
| merchant_id | UUID         | NOT NULL, FK → merchants(id)    | Merchant who created the order                    |
| amount      | INTEGER      | NOT NULL, CHECK (amount >= 100) | Amount in paise (smallest unit)                   |
| currency    | VARCHAR(3)   | DEFAULT 'INR'                   | Currency code                                     |
| receipt     | VARCHAR(255) | NULL                            | Merchant's receipt identifier                     |
| notes       | JSONB        | NULL                            | Additional metadata                               |
| status      | VARCHAR(20)  | DEFAULT 'created'               | Order status                                      |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP       | Order creation time                               |
| updated_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP       | Last update time                                  |

Foreign Keys:

merchant_id references merchants(id)

Indexes:

Primary Key on id

Index on merchant_id (for efficient merchant queries)

Constraints:

amount must be >= 100 (minimum ₹1.00)

Sample Data:

{
  "id": "order_NXhj67fGH2jk9mPq",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50000,
  "currency": "INR",
  "receipt": "receipt_123",
  "notes": {"customer_name": "John Doe"},
  "status": "created",
  "created_at": "2026-01-10T07:30:00Z",
  "updated_at": "2026-01-10T07:30:00Z"
}

3. Payments Table
Stores payment transactions with method-specific details.

Table Name: payments

| Column            | Type         | Constraints                  | Description                                       |
| ----------------- | ------------ | ---------------------------- | ------------------------------------------------- |
| id                | VARCHAR(64)  | PRIMARY KEY                  | Payment identifier (format: pay_XXXXXXXXXXXXXXXX) |
| order_id          | VARCHAR(64)  | NOT NULL, FK → orders(id)    | Associated order                                  |
| merchant_id       | UUID         | NOT NULL, FK → merchants(id) | Merchant who owns the payment                     |
| amount            | INTEGER      | NOT NULL                     | Payment amount in paise                           |
| currency          | VARCHAR(3)   | DEFAULT 'INR'                | Currency code                                     |
| method            | VARCHAR(20)  | NOT NULL                     | Payment method (upi/card)                         |
| status            | VARCHAR(20)  | DEFAULT 'processing'         | Payment status                                    |
| vpa               | VARCHAR(255) | NULL                         | Virtual Payment Address (UPI only)                |
| card_network      | VARCHAR(20)  | NULL                         | Card network (visa/mastercard/amex/rupay)         |
| card_last4        | VARCHAR(4)   | NULL                         | Last 4 digits of card (Card only)                 |
| error_code        | VARCHAR(50)  | NULL                         | Error code if payment failed                      |
| error_description | TEXT         | NULL                         | Error description if payment failed               |
| created_at        | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP    | Payment creation time                             |
| updated_at        | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP    | Last status update time                           |

Foreign Keys:

order_id references orders(id)

merchant_id references merchants(id)

Indexes:

Primary Key on id

Index on order_id (for efficient order queries)

Index on merchant_id (for efficient merchant queries)

Index on status (for status-based queries)

Valid Values:

method: "upi", "card"

status: "processing", "success", "failed"

card_network: "visa", "mastercard", "amex", "rupay", "unknown"

Sample Data (UPI):

{
  "id": "pay_H8sK3jD9s2L1pQr",
  "order_id": "order_NXhj67fGH2jk9mPq",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50000,
  "currency": "INR",
  "method": "upi",
  "status": "success",
  "vpa": "user@paytm",
  "card_network": null,
  "card_last4": null,
  "error_code": null,
  "error_description": null,
  "created_at": "2026-01-10T07:31:00Z",
  "updated_at": "2026-01-10T07:31:10Z"
}


Sample Data card:
{
  "id": "pay_K2mN5pQrS8tUvWxY",
  "order_id": "order_NXhj67fGH2jk9mPq",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50000,
  "currency": "INR",
  "method": "card",
  "status": "success",
  "vpa": null,
  "card_network": "visa",
  "card_last4": "1111",
  "error_code": null,
  "error_description": null,
  "created_at": "2026-01-10T07:32:00Z",
  "updated_at": "2026-01-10T07:32:05Z"
}


Entity Relationships

┌─────────────┐
│  merchants  │
│  (UUID)     │
└──────┬──────┘
       │
       │ 1:N
       │
       ├─────────────────────┐
       │                     │
       ↓                     ↓
┌─────────────┐       ┌──────────────┐
│   orders    │       │   payments   │
│  (VARCHAR)  │←──────│  (VARCHAR)   │
└─────────────┘  N:1  └──────────────┘


Relationships:

One merchant can have many orders (1:N)

One merchant can have many payments (1:N)

One order can have many payments (1:N)

Each payment belongs to one order (N:1)

Each payment belongs to one merchant (N:1)

Database Seeding
The application automatically seeds a test merchant on startup:

INSERT INTO merchants (
  id, 
  name, 
  email, 
  api_key, 
  api_secret, 
  created_at, 
  updated_at
) 
SELECT 
  '550e8400-e29b-41d4-a716-446655440000',
  'Test Merchant',
  'test@example.com',
  'key_test_abc123',
  'secret_test_xyz789',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM merchants WHERE email = 'test@example.com'
);
Payment Status Flow

Order Created
     ↓
Payment Initiated
     ↓
  processing ──→ success
             └──→ failed


Note: Payments are created directly with processing status. The created state is skipped.

Amount Storage
All amounts are stored in paise (smallest currency unit):

₹1.00 = 100 paise

₹500.00 = 50000 paise

₹10,000.00 = 1000000 paise

This prevents floating-point precision issues with decimal currency values.

Security Considerations
What is Stored:
✅ Order IDs (non-sensitive)

✅ Payment IDs (non-sensitive)

✅ UPI VPA (user@bank format)

✅ Card network (visa/mastercard/amex/rupay)

✅ Last 4 digits of card

What is NOT Stored:
❌ Full card numbers

❌ CVV codes

❌ Card expiry dates

❌ Cardholder names

| Table     | Index               | Purpose                            |
| --------- | ------------------- | ---------------------------------- |
| merchants | PRIMARY KEY (id)    | Unique merchant lookup             |
| merchants | UNIQUE (email)      | Prevent duplicate emails           |
| merchants | UNIQUE (api_key)    | Prevent duplicate API keys         |
| orders    | PRIMARY KEY (id)    | Unique order lookup                |
| orders    | INDEX (merchant_id) | Efficient merchant order queries   |
| payments  | PRIMARY KEY (id)    | Unique payment lookup              |
| payments  | INDEX (order_id)    | Efficient order payment queries    |
| payments  | INDEX (merchant_id) | Efficient merchant payment queries |
| payments  | INDEX (status)      | Status-based filtering             |

Query Examples
Find all successful payments for a merchant:

SELECT * FROM payments 
WHERE merchant_id = '550e8400-e29b-41d4-a716-446655440000' 
  AND status = 'success'
ORDER BY created_at DESC;


Get order with all payments:
SELECT o.*, p.* 
FROM orders o
LEFT JOIN payments p ON o.id = p.order_id
WHERE o.id = 'order_NXhj67fGH2jk9mPq';


Calculate merchant statistics:

SELECT 
  merchant_id,
  COUNT(*) as total_transactions,
  SUM(CASE WHEN status = 'success' THEN amount ELSE 0 END) as total_amount,
  ROUND(100.0 * SUM(CASE WHEN status = 'success' THEN 1 ELSE 0 END) / COUNT(*), 2) as success_rate
FROM payments
WHERE merchant_id = '550e8400-e29b-41d4-a716-446655440000'
GROUP BY merchant_id;

