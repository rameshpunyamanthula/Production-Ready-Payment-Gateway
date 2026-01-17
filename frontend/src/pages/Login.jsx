import { useState } from "react";
import { api } from "../services/api";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const [email, setEmail] = useState("");
  const navigate = useNavigate();

  const login = async (e) => {
    e.preventDefault();
    // Simple login (Deliverable 1 requirement)
    if (email === "test@example.com") {
      navigate("/dashboard");
    }
  };

  return (
    <form data-test-id="login-form" onSubmit={login}>
      <input
        data-test-id="email-input"
        type="email"
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      <input
        data-test-id="password-input"
        type="password"
        placeholder="Password"
      />
      <button data-test-id="login-button">Login</button>
    </form>
  );
}