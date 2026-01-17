import { useEffect, useState } from "react";
import { createOrder, getAllPayments } from "../services/api";

export default function Dashboard() {
  const [stats, setStats] = useState({
    total: 0,
    amount: 0,
    successRate: 0,
  });
  
  // Create Order Form
  const [amount, setAmount] = useState("");
  const [orderId, setOrderId] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Fetch real stats from backend
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const payments = await getAllPayments();
      
      const total = payments.length;
      const successfulPayments = payments.filter(p => p.status === "success");
      const totalAmount = successfulPayments.reduce((sum, p) => sum + p.amount, 0);
      const successRate = total > 0 ? Math.round((successfulPayments.length / total) * 100) : 0;

      setStats({
        total: total,
        amount: totalAmount,
        successRate: successRate,
      });
    } catch (err) {
      console.error("Error fetching stats:", err);
      // Fallback to placeholder if backend fails
      setStats({
        total: 0,
        amount: 0,
        successRate: 0,
      });
    }
  };

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    setLoading(true);
    setOrderId("");

    try {
      const order = await createOrder(parseInt(amount), "INR");
      setOrderId(order.id);
      setAmount("");
      // Refresh stats after creating order
      fetchStats();
    } catch (err) {
      alert("Error creating order: " + (err.response?.data?.error?.description || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div data-test-id="dashboard">
      <div style={{ marginBottom: "30px" }}>
  <h1>Merchant Dashboard</h1>
    <div style={{ marginTop: "15px", display: "flex", gap: "15px" }}>
    <a
      href="/dashboard"
      style={{
        padding: "10px 20px",
        background: "#667eea",
        color: "white",
        textDecoration: "none",
        borderRadius: "8px",
        fontWeight: "bold",
      }}
    >
      Dashboard
    </a>
    <a
      href="/dashboard/transactions"
      style={{
        padding: "10px 20px",
        background: "#764ba2",
        color: "white",
        textDecoration: "none",
        borderRadius: "8px",
        fontWeight: "bold",
      }}
    >
      Transactions
    </a>
    <a
      href="/dashboard/webhooks"
      style={{
        padding: "10px 20px",
        background: "#16a34a",
        color: "white",
        textDecoration: "none",
        borderRadius: "8px",
        fontWeight: "bold",
      }}
    >
      Webhooks
    </a>
    <a
      href="/dashboard/docs"
      style={{
        padding: "10px 20px",
        background: "#0ea5e9",
        color: "white",
        textDecoration: "none",
        borderRadius: "8px",
        fontWeight: "bold",
      }}
    >
      API Docs
    </a>
  </div>

</div>


      <div data-test-id="api-credentials">
        <div>
          <label>API Key</label>
          <span data-test-id="api-key">key_test_abc123</span>
        </div>
        <div>
          <label>API Secret</label>
          <span data-test-id="api-secret">secret_test_xyz789</span>
        </div>
      </div>

      {/* CREATE ORDER FORM */}
      <div style={{ margin: "30px 0", padding: "30px", background: "white", borderRadius: "12px" }}>
        <h2>Create New Order</h2>
        <form onSubmit={handleCreateOrder} style={{ marginTop: "20px" }}>
          <input
            type="number"
            placeholder="Amount in paise (min 100)"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
            min="100"
            style={{ marginBottom: "15px" }}
          />
          <button type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create Order"}
          </button>
        </form>
        
        {orderId && (
          <div style={{ marginTop: "20px", padding: "20px", background: "#d4edda", borderRadius: "8px" }}>
            <p style={{ fontWeight: "bold", marginBottom: "10px" }}>✅ Order Created!</p>
            <p style={{ fontSize: "14px", marginBottom: "15px", fontFamily: "monospace" }}>
              Order ID: {orderId}
            </p>
            <a
              href={`http://localhost:3001/checkout?order_id=${orderId}`}
              target="_blank"
              rel="noreferrer"
              style={{
                display: "inline-block",
                padding: "12px 24px",
                background: "#28a745",
                color: "white",
                textDecoration: "none",
                borderRadius: "8px",
                fontWeight: "bold",
              }}
            >
              Open Checkout Page →
            </a>
          </div>
        )}
      </div>

      <div data-test-id="stats-container">
        <div data-test-id="total-transactions">{stats.total}</div>
        <div data-test-id="total-amount">₹{stats.amount}</div>
        <div data-test-id="success-rate">{stats.successRate}%</div>
      </div>
    </div>
  );
}
