import { useEffect, useState } from "react";
import { api } from "../services/api";

export default function Checkout() {
  const params = new URLSearchParams(window.location.search);
  const orderId = params.get("order_id");

  const [order, setOrder] = useState(null);
  const [method, setMethod] = useState(null);
  const [vpa, setVpa] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");
  const [cardholderName, setCardholderName] = useState("");
  const [state, setState] = useState("idle");
  const [paymentData, setPaymentData] = useState(null);

  // Fetch order
  useEffect(() => {
    if (!orderId) return;
    api.get(`/api/v1/orders/${orderId}/public`)
      .then(res => setOrder(res.data))
      .catch(() => setState("failed"));
  }, [orderId]);

  // Poll payment status
  useEffect(() => {
    if (!paymentData?.id) return;
    
    const pollInterval = setInterval(async () => {
      try {
        const res = await api.get(`/api/v1/payments/${paymentData.id}/public`);
        if (res.data.status === "success") {
          clearInterval(pollInterval);
          setState("success");
          setPaymentData(prev => ({...prev, status: "success"}));
        } else if (res.data.status === "failed") {
          clearInterval(pollInterval);
          setState("failed");
        }
      } catch (err) {
        console.error("Poll error:", err);
      }
    }, 2000);

    return () => clearInterval(pollInterval);
  }, [paymentData?.id]);

  const payUPI = async (e) => {
    e.preventDefault();
    if (!vpa.trim()) {
      alert("Enter valid UPI ID");
      return;
    }

    setState("processing");

    try {
      const res = await api.post("/api/v1/payments/public", {
        order_id: orderId,
        method: "upi",
        vpa,
      });

      const pId = res.data.payment_id || res.data.id;
      const pStatus = res.data.status;

      setPaymentData({ id: pId, status: pStatus });

      if (pStatus === "success") {
        setState("success");
      }
    } catch (err) {
      console.error("Payment error:", err);
      setState("failed");
    }
  };

  const payCard = async (e) => {
    e.preventDefault();

    setState("processing");

    try {
      const [month, year] = expiry.split("/");

      const res = await api.post("/api/v1/payments/public", {
        order_id: orderId,
        method: "card",
        card: {
          number: cardNumber.replace(/\s/g, ""),
          expiry_month: month?.trim(),
          expiry_year: year?.trim(),
          cvv: cvv,
          holder_name: cardholderName
        }
      });

      const pId = res.data.payment_id || res.data.id;
      const pStatus = res.data.status;

      setPaymentData({ id: pId, status: pStatus });

      if (pStatus === "success") {
        setState("success");
      }
    } catch (err) {
      console.error("Payment error:", err);
      setState("failed");
    }
  };

  if (!order) return <div>Loading order...</div>;

  return (
    <div data-test-id="checkout-container" className="checkout-container">
      <div data-test-id="order-summary" className="order-summary">
        <h2>Complete Payment</h2>
        <div>
          <span>Amount: </span>
          <span data-test-id="order-amount">₹{order.amount / 100}</span>
        </div>
        <div>
          <span>Order ID: </span>
          <span data-test-id="order-id">{order.id}</span>
        </div>
      </div>

      {state === "idle" && (
        <>
          <div data-test-id="payment-methods" className="payment-methods">
            <button 
              data-test-id="method-upi"
              data-method="upi"
              onClick={() => setMethod("upi")}
              className={method === "upi" ? "active" : ""}
            >
              UPI
            </button>
            <button 
              data-test-id="method-card"
              data-method="card"
              onClick={() => setMethod("card")}
              className={method === "card" ? "active" : ""}
            >
              Card
            </button>
          </div>

          {method === "upi" && (
            <form data-test-id="upi-form" onSubmit={payUPI}>
              <input
                data-test-id="vpa-input"
                placeholder="username@bank"
                type="text"
                value={vpa}
                onChange={(e) => setVpa(e.target.value)}
                required
              />
              <button data-test-id="pay-button" type="submit">
                Pay ₹{order.amount / 100}
              </button>
            </form>
          )}

          {method === "card" && (
            <form data-test-id="card-form" onSubmit={payCard}>
              <input
                data-test-id="card-number-input"
                placeholder="Card Number"
                type="text"
                value={cardNumber}
                onChange={(e) => setCardNumber(e.target.value)}
                required
              />
              <input
                data-test-id="expiry-input"
                placeholder="MM/YY"
                type="text"
                value={expiry}
                onChange={(e) => setExpiry(e.target.value)}
                required
              />
              <input
                data-test-id="cvv-input"
                placeholder="CVV"
                type="text"
                value={cvv}
                onChange={(e) => setCvv(e.target.value)}
                maxLength="3"
                required
              />
              <input
                data-test-id="cardholder-name-input"
                placeholder="Name on Card"
                type="text"
                value={cardholderName}
                onChange={(e) => setCardholderName(e.target.value)}
                required
              />
              <button data-test-id="pay-button" type="submit">
                Pay ₹{order.amount / 100}
              </button>
            </form>
          )}
        </>
      )}

      {state === "processing" && (
        <div data-test-id="processing-state" className="processing-state">
          <div className="spinner"></div>
          <span data-test-id="processing-message">
            Processing payment...
          </span>
        </div>
      )}

      {state === "success" && paymentData && (
        <div data-test-id="success-state" className="success-state">
          <h2>Payment Successful!</h2>
          <div>
            <span>Payment ID: </span>
            <span data-test-id="payment-id">{paymentData.id}</span>
          </div>
          <span data-test-id="success-message">
            Your payment has been processed successfully
          </span>
        </div>
      )}

      {state === "failed" && (
        <div data-test-id="error-state" className="error-state">
          <h2>Payment Failed</h2>
          <span data-test-id="error-message">
            Payment could not be processed
          </span>
          <button 
            data-test-id="retry-button"
            onClick={() => {
              setState("idle");
              setPaymentData(null);
              setMethod(null);
              setVpa("");
            }}
          >
            Try Again
          </button>
        </div>
      )}
    </div>
  );
}
