import { openModal, closeModal } from './modal';
import './styles.css';

class PaymentGateway {
  constructor(options) {
    if (!options || typeof options !== 'object') {
      throw new Error('PaymentGateway: options object is required');
    }

    const { key, orderId, onSuccess, onFailure, onClose } = options;

    if (!key) {
      throw new Error('PaymentGateway: "key" is required');
    }
    if (!orderId) {
      throw new Error('PaymentGateway: "orderId" is required');
    }

    this.key = key;
    this.orderId = orderId;
    this.onSuccess = typeof onSuccess === 'function' ? onSuccess : () => {};
    this.onFailure = typeof onFailure === 'function' ? onFailure : () => {};
    this.onClose = typeof onClose === 'function' ? onClose : () => {};

    this.iframeOrigin = 'http://localhost:3001'; // checkout origin
    this.messageHandler = this.handleMessage.bind(this);
  }

  open() {
    const url = `${this.iframeOrigin}/checkout?order_id=${encodeURIComponent(
      this.orderId
    )}&embedded=true`;

    openModal(url, () => {
      this.onClose();
    });

    window.addEventListener('message', this.messageHandler);
  }

  close() {
    closeModal();
    window.removeEventListener('message', this.messageHandler);
    this.onClose();
  }

  handleMessage(event) {
    if (!event.data || !event.data.type) {
      return;
    }

    const { type, data } = event.data;

    if (type === 'payment_success') {
      this.onSuccess(data);
      this.close();
    } else if (type === 'payment_failed') {
      this.onFailure(data);
    } else if (type === 'close_modal') {
      this.close();
    }
  }
}

window.PaymentGateway = PaymentGateway;
export default PaymentGateway;
