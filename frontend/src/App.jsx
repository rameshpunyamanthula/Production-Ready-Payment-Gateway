import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Transactions from "./pages/Transactions";
import Webhooks from "./pages/Webhooks";
import ApiDocs from "./pages/ApiDocs";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Default route */}
        <Route path="/" element={<Navigate to="/login" />} />

        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/dashboard/transactions" element={<Transactions />} />
        <Route path="/dashboard/webhooks" element={<Webhooks />} />
        <Route path="/dashboard/docs" element={<ApiDocs />} />
      </Routes>
    </BrowserRouter>
  );
}
