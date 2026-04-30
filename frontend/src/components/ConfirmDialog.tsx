import { Modal } from './Modal';

interface ConfirmDialogProps {
  title: string;
  text: string;
  isBusy?: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export function ConfirmDialog({ title, text, isBusy, onCancel, onConfirm }: ConfirmDialogProps) {
  return (
    <Modal title={title} onClose={onCancel}>
      <p className="confirm-text">{text}</p>
      <div className="form-actions">
        <button type="button" className="ghost-button" onClick={onCancel}>
          Отмена
        </button>
        <button type="button" className="danger-button" disabled={isBusy} onClick={onConfirm}>
          Удалить
        </button>
      </div>
    </Modal>
  );
}
