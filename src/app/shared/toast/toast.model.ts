import { EscenaIlustracion } from '../ilustracion/ilustracion.component';

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
  /** Ilustración animada: en confirmaciones siempre hay una (por defecto
   *  'documento'); en éxitos solo si se pide, y reemplaza al ícono. */
  escena?: EscenaIlustracion;
}
