export default function ApiDocs() {
  const baseUrl = "http://localhost:8000";

  const orderExample = `
curl -X POST ${baseUrl}/api/v1/orders \\
  -H "X-Api-Key: key_test_abc123" \\
  -H "X-Api-Secret: secret_test_xyz789" \\
  -H "Content-Type: application/json" \\
  -d '{
    "amount": 50000,
    "currency": "INR",
    "receipt": "receipt_123"
  }'
`.trim();

  const paymentExample = `
curl -X POST ${baseUrl}/api/v1/payments \\
  -H "X-Api-Key: key_test_abc123" \\
  -H "X-Api-Secret: secret_test_xyz789" \\
  -H "Idempotency-Key: pay_req_1" \\
  -H "Content-Type: application/json" \\
  -d '{
    "order_id": "order_xxx",
    "method": "upi",
    "vpa": "user@upi"
  }'
`.trim();

  const refundExample = `
curl -X POST ${baseUrl}/api/v1/payments/{payment_id}/refunds \\
  -H "X-Api-Key: key_test_abc123" \\
  -H "X-Api-Secret: secret_test_xyz789" \\
  -H "Content-Type: application/json" \\
  -d '{
    "amount": 50000,
    "reason": "Customer requested refund"
  }'
`.trim();

  const sdkExample = `
<script src="http://localhost:3001/checkout.js"></script>

<button id="pay-button">Pay Now</button>

<script>
  document.getElementById('pay-button').addEventListener('click', function () {
    const checkout = new PaymentGateway({
      key: 'key_test_abc123',
      orderId: 'order_xxx',
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
`.trim();

  return (
    <div data-test-id="api-docs" style={{ padding: "24px" }}>
      <h1>API & SDK Documentation</h1>

      <section style={{ marginTop: "24px" }}>
        <h2>1. Create Order</h2>
        <pre
          data-test-id="create-order-example"
          style={{
            background: "#0f172a",
            color: "#e5e7eb",
            padding: "12px",
            borderRadius: "6px",
            overflowX: "auto",
            fontSize: "13px",
          }}
        >
{orderExample}
        </pre>
      </section>

      <section style={{ marginTop: "24px" }}>
        <h2>2. Create Payment</h2>
        <pre
          data-test-id="create-payment-example"
          style={{
            background: "#0f172a",
            color: "#e5e7eb",
            padding: "12px",
            borderRadius: "6px",
            overflowX: "auto",
            fontSize: "13px",
          }}
        >
{paymentExample}
        </pre>
      </section>

      <section style={{ marginTop: "24px" }}>
        <h2>3. Create Refund</h2>
        <pre
          data-test-id="create-refund-example"
          style={{
            background: "#0f172a",
            color: "#e5e7eb",
            padding: "12px",
            borderRadius: "6px",
            overflowX: "auto",
            fontSize: "13px",
          }}
        >
{refundExample}
        </pre>
      </section>

      <section style={{ marginTop: "24px" }}>
        <h2>4. Checkout SDK Usage</h2>
        <pre
          data-test-id="sdk-example"
          style={{
            background: "#0f172a",
            color: "#e5e7eb",
            padding: "12px",
            borderRadius: "6px",
            overflowX: "auto",
            fontSize: "13px",
          }}
        >
{sdkExample}
        </pre>
      </section>
    </div>
  );
}
