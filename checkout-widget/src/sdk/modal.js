const MODAL_ID = 'payment-gateway-modal';

export function openModal(iframeSrc, onClose) {
  if (document.getElementById(MODAL_ID)) {
    return;
  }

  const modal = document.createElement('div');
  modal.id = MODAL_ID;
  modal.setAttribute('data-test-id', 'payment-modal');

  modal.innerHTML = `
    <div class="pg-modal-overlay">
      <div class="pg-modal-content">
        <iframe
          data-test-id="payment-iframe"
          src="${iframeSrc}"
          frameborder="0"
        ></iframe>
        <button
          data-test-id="close-modal-button"
          class="pg-close-button"
          type="button"
        >
          ×
        </button>
      </div>
    </div>
  `;

  document.body.appendChild(modal);

  const closeButton = modal.querySelector('[data-test-id="close-modal-button"]');
  closeButton.addEventListener('click', () => {
    closeModal();
    if (typeof onClose === 'function') {
      onClose();
    }
  });
}

export function closeModal() {
  const modal = document.getElementById(MODAL_ID);
  if (modal && modal.parentNode) {
    modal.parentNode.removeChild(modal);
  }
}
