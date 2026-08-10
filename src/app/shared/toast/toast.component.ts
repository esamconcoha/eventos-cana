import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, transition, style, animate, keyframes } from '@angular/animations';
import { ToastService } from './toast.service';
import { Toast } from './toast.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css',
  animations: [
    // Animación principal del toast: entrada desde arriba con bounce y salida hacia arriba
    trigger('toastAnim', [
      transition(':enter', [
        style({ 
          opacity: 0, 
          transform: 'translateY(-20px) scale(0.9)',
          filter: 'blur(4px)'
        }),
        animate('350ms cubic-bezier(0.34, 1.56, 0.64, 1)', 
          style({ 
            opacity: 1, 
            transform: 'translateY(0) scale(1)',
            filter: 'blur(0px)'
          })
        )
      ]),
      transition(':leave', [
        animate('200ms cubic-bezier(0.4, 0, 1, 1)', 
          style({ 
            opacity: 0, 
            transform: 'translateY(-16px) scale(0.95)',
            filter: 'blur(2px)'
          })
        )
      ])
    ]),

    // Animación del check (success) - dibujado progresivo
    trigger('checkAnim', [
      transition(':enter', [
        style({ 
          strokeDasharray: '50',
          strokeDashoffset: '50',
          opacity: 0,
          transform: 'scale(0.5) rotate(-45deg)'
        }),
        animate('500ms cubic-bezier(0.34, 1.56, 0.64, 1)', 
          keyframes([
            style({ strokeDashoffset: '50', opacity: 0, transform: 'scale(0.5) rotate(-45deg)', offset: 0 }),
            style({ strokeDashoffset: '0', opacity: 1, transform: 'scale(1.1) rotate(0deg)', offset: 0.7 }),
            style({ strokeDashoffset: '0', opacity: 1, transform: 'scale(1) rotate(0deg)', offset: 1 })
          ])
        )
      ])
    ]),

    // Animación del error - shake horizontal
    trigger('errorShake', [
      transition(':enter', [
        animate('600ms cubic-bezier(0.36, 0.07, 0.19, 0.97)', 
          keyframes([
            style({ transform: 'translateX(0) scale(0.5)', opacity: 0, offset: 0 }),
            style({ transform: 'translateX(0) scale(1)', opacity: 1, offset: 0.2 }),
            style({ transform: 'translateX(-8px) rotate(-5deg)', offset: 0.4 }),
            style({ transform: 'translateX(8px) rotate(5deg)', offset: 0.6 }),
            style({ transform: 'translateX(-4px) rotate(-2deg)', offset: 0.8 }),
            style({ transform: 'translateX(0) rotate(0deg)', offset: 1 })
          ])
        )
      ])
    ]),

    // Animación del warning - pulse
    trigger('pulseAnim', [
      transition(':enter', [
        animate('800ms ease-out', 
          keyframes([
            style({ transform: 'scale(0)', opacity: 0, offset: 0 }),
            style({ transform: 'scale(1.2)', opacity: 1, offset: 0.5 }),
            style({ transform: 'scale(0.95)', offset: 0.7 }),
            style({ transform: 'scale(1.05)', offset: 0.85 }),
            style({ transform: 'scale(1)', offset: 1 })
          ])
        )
      ])
    ]),

    // Animación del info - fade in simple
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'scale(0.7)' }),
        animate('300ms cubic-bezier(0.34, 1.56, 0.64, 1)', 
          style({ opacity: 1, transform: 'scale(1)' })
        )
      ])
    ]),

    // Progress bar animado
    trigger('progressBar', [
      transition(':enter', [
        style({ width: '100%' }),
        animate('{{ duration }}ms linear', style({ width: '0%' }))
      ])
    ])
  ]
})
export class ToastComponent {
  toasts$!: Observable<Toast[]>;

  constructor(private toastService: ToastService) {
      this.toasts$ = this.toastService.toasts$;
  }
   

  dismiss(toast: Toast): void {
    this.toastService.remove(toast.id);
  }

  confirm(toast: Toast): void {
    toast.onConfirm?.();
    this.toastService.remove(toast.id);
  }

  cancel(toast: Toast): void {
    toast.onCancel?.();
    this.toastService.remove(toast.id);
  }

  trackById(_: number, t: Toast): string {
    return t.id;
  }
  

  // Backgrounds con gradientes y blur
  bgFor(type: string): string {
    const bgs: Record<string, string> = {
      success: 'bg-gradient-to-br from-green-50 to-emerald-100 border-2 border-green-200',
      error:   'bg-gradient-to-br from-red-50 to-rose-100 border-2 border-red-200',
      warning: 'bg-gradient-to-br from-amber-50 to-yellow-100 border-2 border-amber-200',
      info:    'bg-gradient-to-br from-blue-50 to-cyan-100 border-2 border-blue-200'
    };
    return bgs[type] ?? 'bg-white border-2 border-gray-200';
  }

  // Contenedor del icono con fondo circular
  iconContainerFor(type: string): string {
    const containers: Record<string, string> = {
      success: 'w-10 h-10 rounded-full bg-green-500 text-white flex items-center justify-center shadow-lg shadow-green-200',
      error:   'w-10 h-10 rounded-full bg-red-500 text-white flex items-center justify-center shadow-lg shadow-red-200',
      warning: 'w-10 h-10 rounded-full bg-amber-500 text-white flex items-center justify-center shadow-lg shadow-amber-200',
      info:    'w-10 h-10 rounded-full bg-blue-500 text-white flex items-center justify-center shadow-lg shadow-blue-200'
    };
    return containers[type] ?? 'w-10 h-10 rounded-full bg-gray-500 text-white flex items-center justify-center';
  }

  // Colores de título
  titleColorFor(type: string): string {
    const colors: Record<string, string> = {
      success: 'text-green-900',
      error:   'text-red-900',
      warning: 'text-amber-900',
      info:    'text-blue-900'
    };
    return colors[type] ?? 'text-gray-900';
  }

  // Colores de mensaje
  messageColorFor(type: string): string {
    const colors: Record<string, string> = {
      success: 'text-green-700',
      error:   'text-red-700',
      warning: 'text-amber-700',
      info:    'text-blue-700'
    };
    return colors[type] ?? 'text-gray-700';
  }

  // Botón de cerrar
  closeButtonFor(type: string): string {
    const buttons: Record<string, string> = {
      success: 'text-green-600 hover:bg-green-200/50',
      error:   'text-red-600 hover:bg-red-200/50',
      warning: 'text-amber-600 hover:bg-amber-200/50',
      info:    'text-blue-600 hover:bg-blue-200/50'
    };
    return buttons[type] ?? 'text-gray-600 hover:bg-gray-200/50';
  }

  // Color del progress bar
  progressColorFor(type: string): string {
    const colors: Record<string, string> = {
      success: 'bg-green-500',
      error:   'bg-red-500',
      warning: 'bg-amber-500',
      info:    'bg-blue-500'
    };
    return colors[type] ?? 'bg-gray-500';
  }
}
