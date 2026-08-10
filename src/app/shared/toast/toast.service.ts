import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Toast, ToastType } from './toast.model';

@Injectable({ providedIn: 'root' })
export class ToastService {

  private _toasts = new BehaviorSubject<Toast[]>([]);
  toasts$ = this._toasts.asObservable();

  private uid(): string {
    return Math.random().toString(36).slice(2, 9);
  }

  private add(toast: Omit<Toast, 'id' | 'removing'>): void {
    const t: Toast = { ...toast, id: this.uid(), removing: false };
    this._toasts.next([...this._toasts.value, t]);

    if (!toast.confirm && (toast.duration ?? 3000) > 0) {
      setTimeout(() => this.remove(t.id), toast.duration ?? 3000);
    }
  }

  remove(id: string): void {
    // Marca como "removing" para disparar animación de salida
    this._toasts.next(
      this._toasts.value.map(t => t.id === id ? { ...t, removing: true } : t)
    );
    // Elimina del array después de que termine la animación (300ms)
    setTimeout(() => {
      this._toasts.next(this._toasts.value.filter(t => t.id !== id));
    }, 300);
  }

  success(title: string, message?: string): void {
    this.add({ type: 'success', title, message, duration: 3000 });
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
  }): void {
    this.add({
      type: 'warning',
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
