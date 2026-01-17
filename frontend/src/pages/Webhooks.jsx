import { useEffect, useState } from "react";
import { api } from "../services/api";

const API_KEY = "key_test_abc123";
const API_SECRET = "secret_test_xyz789";

export default function Webhooks() {
  const [webhookUrl, setWebhookUrl] = useState("");
  const [webhookSecret, setWebhookSecret] = useState("");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    fetchWebhookConfig();
  }, []);

  const fetchWebhookConfig = async () => {
    try {
      const res = await api.get("/api/v1/merchant/webhook", {
        headers: {
          "X-Api-Key": API_KEY,
          "X-Api-Secret": API_SECRET,
        },
      });
      setWebhookUrl(res.data.webhook_url || "");
      setWebhookSecret(res.data.webhook_secret || "");
    } catch (err) {
      console.error("Failed to load webhook config", err);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage("");

    try {
      await api.post(
        "/api/v1/merchant/webhook",
        { webhook_url: webhookUrl },
        {
          headers: {
            "X-Api-Key": API_KEY,
            "X-Api-Secret": API_SECRET,
          },
        }
      );
      setMessage("Webhook URL updated successfully");
      await fetchWebhookConfig();
    } catch (err) {
      console.error("Failed to update webhook", err);
      setMessage("Failed to update webhook URL");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div data-test-id="webhook-settings" style={{ padding: "24px" }}>
      <h1>Webhook Settings</h1>

      <form onSubmit={handleSave} style={{ marginTop: "24px", maxWidth: "520px" }}>
        <div style={{ marginBottom: "16px" }}>
          <label htmlFor="webhook-url">Webhook URL</label>
          <input
            id="webhook-url"
            data-test-id="webhook-url-input"
            type="url"
            value={webhookUrl}
            onChange={(e) => setWebhookUrl(e.target.value)}
            placeholder="https://yourapp.com/webhook"
            required
            style={{ width: "100%", padding: "8px", marginTop: "4px" }}
          />
        </div>

        <div style={{ marginBottom: "16px" }}>
          <label>Webhook Secret (read-only)</label>
          <div
            data-test-id="webhook-secret"
            style={{
              marginTop: "4px",
              padding: "8px",
              background: "#f3f4f6",
              borderRadius: "4px",
              fontFamily: "monospace",
            }}
          >
            {webhookSecret || "—"}
          </div>
        </div>

        <button
          type="submit"
          data-test-id="save-webhook-button"
          disabled={saving}
          style={{
            padding: "10px 20px",
            background: "#16a34a",
            color: "white",
            border: "none",
            borderRadius: "6px",
            cursor: "pointer",
          }}
        >
          {saving ? "Saving..." : "Save Webhook URL"}
        </button>

        {message && (
          <p style={{ marginTop: "12px", fontSize: "14px" }}>{message}</p>
        )}
      </form>
    </div>
  );
}
