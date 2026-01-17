import { useEffect, useState } from "react";
import { getAllPayments } from "../services/api";

export default function Transactions() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPayments();
  }, []);

  const fetchPayments = async () => {
    try {
      const data = await getAllPayments();
      setPayments(data);
    } catch (err) {
      console.error("Error fetching payments:", err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (timestamp) => {
    if (!timestamp) return "N/A";
    const date = new Date(timestamp);
    return date.toLocaleString("en-IN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false,
    }).replace(",", "");
  };

  if (loading) {
    return <div style={{ padding: "40px", textAlign: "center" }}>Loading transactions...</div>;
  }

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "30px" }}>
  <h1>Transactions</h1>
  <div style={{ marginTop: "15px", display: "flex", gap: "15px" }}>
    <a 
      href="/dashboard" 
      style={{ 
        padding: "10px 20px", 
        background: "#667eea", 
        color: "white", 
        textDecoration: "none", 
        borderRadius: "8px",
        fontWeight: "bold"
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
        fontWeight: "bold"
      }}
    >
      Transactions
    </a>
  </div>
</div>

      
      <table data-test-id="transactions-table">
        <thead>
          <tr>
            <th>Payment ID</th>
            <th>Order ID</th>
            <th>Amount</th>
            <th>Method</th>
            <th>Status</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {payments.length === 0 ? (
            <tr>
              <td colSpan="6" style={{ textAlign: "center", padding: "40px" }}>
                No transactions yet
              </td>
            </tr>
          ) : (
            payments.map((payment) => (
              <tr 
                key={payment.id} 
                data-test-id="transaction-row" 
                data-payment-id={payment.id}
              >
                <td data-test-id="payment-id">{payment.id}</td>
                <td data-test-id="order-id">{payment.order_id}</td>
                <td data-test-id="amount">{payment.amount}</td>
                <td data-test-id="method">{payment.method}</td>
                <td data-test-id="status">{payment.status}</td>
                <td data-test-id="created-at">{formatDate(payment.created_at)}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
