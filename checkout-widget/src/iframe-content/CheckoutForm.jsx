import React from 'react';

function sendMessageToParent(type, data) {
  window.parent.postMessage(
    {
      type,
      data
    },
    '*'
  );
}

export default function CheckoutForm() {
  const handleSuccess = () => {
    sendMessageToParent('payment_success', { paymentId: 'demo_payment_id' });
  };

  const handleFailure = () => {
    sendMessageToParent('payment_failed', { error: 'demo_error' });
  };

  const handleClose = () => {
    sendMessageToParent('close_modal', {});
  };

  return (
    <div>
      <h2>Demo Checkout Form</h2>
      <button onClick={handleSuccess}>Simulate Success</button>
      <button onClick={handleFailure}>Simulate Failure</button>
      <button onClick={handleClose}>Close</button>
    </div>
  );
}
