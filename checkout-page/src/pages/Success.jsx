import { useLocation } from "react-router-dom";

export default function Success() {
  const { state } = useLocation();

  return (
    <div data-test-id="success-state">
      <h2>Payment Successful!</h2>
      <div>
        <span>Payment ID: </span>
        <span data-test-id="payment-id">{state?.id || "N/A"}</span>
      </div>
      <span data-test-id="success-message">
        Your payment has been processed successfully
      </span>
    </div>
  );
}
