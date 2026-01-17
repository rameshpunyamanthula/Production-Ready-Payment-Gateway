import axios from "axios";

export const api = axios.create({
  baseURL: "http://localhost:8000",
  timeout: 10000,
});

// API credentials
const API_KEY = "key_test_abc123";
const API_SECRET = "secret_test_xyz789";

// Create Order
export const createOrder = async (amount, currency = "INR", receipt = "") => {
  const res = await api.post(
    "/api/v1/orders",
    {
      amount: amount,
      currency: currency,
      receipt: receipt || `receipt_${Date.now()}`,
    },
    {
      headers: {
        "X-Api-Key": API_KEY,
        "X-Api-Secret": API_SECRET,
      },
    }
  );
  return res.data;
};

// Get All Payments
export const getAllPayments = async () => {
  const res = await api.get("/api/v1/payments", {
    headers: {
      "X-Api-Key": API_KEY,
      "X-Api-Secret": API_SECRET,
    },
  });
  return res.data;
};
