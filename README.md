# Payment Gateway System

A production-ready payment gateway system supporting UPI and Card payments, similar to Razorpay/Stripe.

## 🚀 Quick Start

Start all services with a single command:

```bash
docker-compose up -d

Wait 10-15 seconds for services to initialize, then access:

API: http://localhost:8000

Dashboard: http://localhost:3000

Checkout: http://localhost:3001

Job Status: http://localhost:8000/api/v1/test/jobs/status

The Redis-backed worker service is started automatically via docker-compose and is required for async payment and refund processing.


📦 Services
PostgreSQL (port 5432): Database with auto-seeded test merchant

Spring Boot API (port 8000): RESTful API with authentication

React Dashboard (port 3000): Merchant dashboard

React Checkout (port 3001): Hosted checkout page

🔑 Test Credentials
Dashboard Login:

Email: test@example.com

Password: Any password (not validated in Deliverable 1)

API Credentials:

API Key: key_test_abc123

API Secret: secret_test_xyz789

🧪 Testing

1. Health Check

curl http://localhost:8000/health

2. Create Order

curl -X POST http://localhost:8000/api/v1/orders \
  -H "X-Api-Key: key_test_abc123" \
  -H "X-Api-Secret: secret_test_xyz789" \
  -H "Content-Type: application/json" \
  -d '{"amount": 50000, "currency": "INR", "receipt": "test_123"}'

3. Test Checkout

Visit: http://localhost:3001/checkout?order_id=<ORDER_ID_FROM_STEP_2>

UPI Payment:

VPA: test@paytm or any valid format

Card Payment:

Card Number: 4111111111111111

Expiry: 12/25

CVV: 123

Name: Any name

🏗️ Architecture

┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Dashboard  │────▶│  Spring API  │────▶│ PostgreSQL  │
│  (Port 3000)│     │  (Port 8000) │     │ (Port 5432) │
└─────────────┘     └──────────────┘     └─────────────┘
                            ▲
                            │
                    ┌───────┴────────┐
                    │    Checkout    │
                    │   (Port 3001)  │
                    └────────────────┘


  ## ⚙️ Async Processing & Job Queues

- All payments and refunds are processed asynchronously via a Redis-backed job queue.
- The API enqueues jobs into Redis lists (`payment_jobs`, `refund_jobs`, `webhook_jobs`).
- A separate worker service (`gateway_worker`) consumes these queues and updates database state.

### Job Status Endpoint

You can inspect the queue health at:

```bash
curl http://localhost:8000/api/v1/test/jobs/status

Example response:
{
  "pending": 0,
  "processing": 0,
  "completed": 0,
  "failed": 0,
  "worker_status": "running"
}

This documents the async part and job status endpoint.[1]

***

## 3) Add a new “Refunds” subsection under API

In your **🔌 API Endpoints** area, after the payments section, add:

```markdown
### Refund Endpoints

Authenticated:

- `POST /api/v1/payments/{payment_id}/refunds`  
  - Body: `{ "amount": <int>, "reason": "<string>" }`  
  - Creates a refund in `pending` state and enqueues a background job.

- `GET /api/v1/refunds/{refund_id}`  
  - Returns refund details and current status (`pending` / `processed`).

Refunds are processed asynchronously. When total refunded amount reaches the payment amount, the payment status becomes `refunded`.



## 📡 Webhooks

The gateway can notify merchant servers about important events (e.g. `payment.success`, `refund.processed`) via HTTP POST webhooks.[web:71][web:78]

### Configuration

- Configure webhook URL and view secret from the dashboard page: `/dashboard/webhooks`.
- Backend endpoint (used by dashboard):

  - `GET /api/v1/merchant/webhook`  
  - `POST /api/v1/merchant/webhook` body: `{ "webhook_url": "https://example.com/webhook" }`

### Delivery

- Events are enqueued into `webhook_jobs` and processed by the worker.
- Each delivery is logged into the `webhook_logs` table with status, attempts, response code, and body.
- Failed deliveries are retried with exponential backoff until a maximum number of attempts.

### Security (HMAC Signature)

Each webhook request includes an HMAC SHA256 signature header computed using the merchant’s `webhook_secret`:

- Header: `X-Webhook-Signature: <hex-hmac>`  
- Payload: JSON body of the event.

Merchants can verify the signature by recomputing the HMAC using their secret and comparing it to the header value.



📊 Database Schema
Merchants Table:

Test merchant auto-seeded on startup

UUID primary key, unique email and api_key

Orders Table:

Format: order_ + 16 alphanumeric chars

Minimum amount: 100 paise (₹1.00)

Amounts stored in paise

Payments Table:

Format: pay_ + 16 alphanumeric chars

Status flow: processing → success/failed

Supports UPI (with VPA validation) and Card (with Luhn validation)

Additional tables:

- `refunds`: Stores refunds linked to payments and merchants.
- `webhook_logs`: Stores webhook delivery attempts and statuses.
- `idempotency_keys`: Stores idempotency keys to guarantee safe retries for payment requests.




🔌 API Endpoints
Public Endpoints:

GET /health - Health check

GET /api/v1/orders/{id}/public - Get order (no auth)

POST /api/v1/payments/public - Create payment (no auth)

GET /api/v1/payments/{id}/public - Get payment status (no auth)

Authenticated Endpoints:

POST /api/v1/orders - Create order

GET /api/v1/orders/{id} - Get order

POST /api/v1/payments - Create payment

GET /api/v1/payments/{id} - Get payment

🛠️ Tech Stack
Backend: Java Spring Boot, PostgreSQL

Frontend: React, React Router

Deployment: Docker, Docker Compose, Nginx

📝 Features
✅ Dockerized deployment (single command)
✅ RESTful API with authentication
✅ UPI payment with VPA validation
✅ Card payment with Luhn algorithm & network detection
✅ Payment polling (2-second intervals)
✅ Merchant dashboard with API credentials
✅ Transaction history
✅ Hosted checkout page
✅ Auto-seeded test merchant
✅ All required data-test-id attributes

## 🧩 Embeddable SDK (checkout.js)

The checkout widget is exposed as a JavaScript SDK served from the checkout service.

### Usage

Include the SDK in any merchant page:

```html
<script src="http://localhost:3001/checkout.js"></script>

<button id="pay-button">Pay Now</button>

<script>
  document.getElementById('pay-button').addEventListener('click', function () {
    const checkout = new PaymentGateway({
      key: 'key_test_abc123',
      orderId: 'order_xxx', // Replace with a real order_id
      onSuccess: function (response) {
        console.log('Payment successful:', response.paymentId);
      },
      onFailure: function (error) {
        console.log('Payment failed:', error);
      },
      onClose: function () {
        console.log('Checkout closed');
      }
    });

    checkout.open();
  });
</script>


The SDK:

Opens a modal with an iframe pointing to the hosted checkout page.

Communicates back using window.postMessage events:

payment_success

payment_failed

close_modal



This is the SDK integration guide.[4]

***

## 6) Add a “Submission & Evaluation” / “submission.yml” note

Near your existing **📤 SUBMISSION** section, add a small note mentioning `submission.yml`:

```markdown
### Automated Evaluation

This repository includes a `submission.yml` file used for automated evaluation. It defines:

- `setup`: install frontend and checkout dependencies.
- `start`: `docker-compose up -d` to launch API, worker, dashboard, checkout, Postgres, Redis.
- `verify`: basic health checks including the job status endpoint.
- `shutdown`: `docker-compose down`.




🧹 Cleanup
Stop and remove all containers:

docker-compose down

Remove volumes (including database):

docker-compose down -v
📄 License
MIT

text

***

## 🎯 **FINAL TEST (2 minutes):**

### **Test these 3 flows:**

1. **Create Order → Pay with UPI → Success** ✅
2. **Dashboard Login → View Credentials** ✅
3. **Health Check** ✅

***

## 📤 **SUBMISSION:**

1. ✅ Push to GitHub
2. ✅ Include screenshots
3. ✅ Record 2-min video demo
4. ✅ Submit repository URL

```markdown
📚 **[View Complete API Documentation](./API.md)** 

🏗️ **[View System Architecture](./ARCHITECTURE.md)**

💾 **[View Database Schema](./DATABASE.md)**