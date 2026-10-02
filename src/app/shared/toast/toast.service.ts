import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Toast, ToastType } from './toast.model';
import { EscenaIlustracion } from '../ilustracion/ilustracion.component';

@Injectable({ providedIn: 'root' })
export class ToastService {

  private _toasts = new BehaviorSubject<Toast[]>([]);
  toasts$ = this._toasts.asObservable();

  private uid(): string {
    return Math.random().toString(36).slice(2, 9);
  }

  private add(toast: Omit<Toast, 'id'>): void {
    const t: Toast = { ...toast, id: this.uid() };
    this._toasts.next([...this._toasts.value, t]);

    if (!toast.confirm && (toast.duration ?? 3000) > 0) {
      setTimeout(() => this.remove(t.id), toast.duration ?? 3000);
    }
  }

  /**
   * Se quita del arreglo en el acto: la salida la anima el trigger :leave de
   * ToastComponent, que mantiene el elemento en pantalla mientras dura.
   *
   * Antes se esperaban 300ms con una bandera "removing" que la plantilla no
   * usaba: la confirmación seguía entera en pantalla, el aviso de éxito de la
   * acción entraba debajo y luego saltaba al irse la confirmación.
   */
  remove(id: string): void {
    this._toasts.next(this._toasts.value.filter(t => t.id !== id));
  }

  /**
   * Con `escena`, el éxito muestra la ilustración en modo "listo" (la acción
   * se completa en pantalla) en lugar del ícono, y dura un poco más para que
   * la animación alcance a terminar.
   */
  success(title: string, message?: string, escena?: EscenaIlustracion): void {
    this.add({ type: 'success', title, message, escena, duration: escena ? 3800 : 3000 });
  }

  error(title: string, message?: string): void {
    this.add({ type: 'error', title, message, duration: 5000 });
  }

  info(title: string, message?: string): void {
    this.add({ type: 'info', title, message, duration: 3000 });
  }

  warning(title: string, message?: string): void {
    this.add({ type: 'warning', title, message, duration: 4000 });
  }

  confirm(options: {
    title: string;
    message?: string;
    confirmText?: string;
    cancelText?: string;
    onConfirm: () => void;
    onCancel?: () => void;
    /** documento (default): confirmar/finalizar · basurero: eliminar/inactivar/anular · estado: cambios de estado */
    escena?: EscenaIlustracion;
  }): void {
    this.add({
      type: 'warning',
      escena: options.escena ?? 'documento',
      title: options.title,
      message: options.message,
      confirm: true,
      duration: 0,
      confirmText: options.confirmText ?? 'Confirmar',
      cancelText: options.cancelText ?? 'Cancelar',
      onConfirm: options.onConfirm,
      onCancel: options.onCancel
    });
  }
}
