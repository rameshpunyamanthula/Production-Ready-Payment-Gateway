import { useLocation, useNavigate } from "react-router-dom";

export default function Failure() {
  const { state } = useLocation();
  const navigate = useNavigate();

  return (
    <div data-test-id="error-state">
      <h2>Payment Failed</h2>
      <span data-test-id="error-message">
        {state?.error_description || "Payment could not be processed"}
      </span>
      <button data-test-id="retry-button" onClick={() => navigate(-1)}>
        Try Again
      </button>
    </div>
  );
}
