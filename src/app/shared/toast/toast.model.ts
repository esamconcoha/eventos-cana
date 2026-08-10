export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;       // ms, 0 = no auto-dismiss
  confirm?: boolean;       // es modal de confirmación
  onConfirm?: () => void;
  onCancel?: () => void;
  confirmText?: string;
  cancelText?: string;
  removing?: boolean;      // flag para animación de salida
}
