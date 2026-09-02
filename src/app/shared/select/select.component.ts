import { Component, Input, ElementRef, Renderer2, ViewChild, OnDestroy, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/**
 * Selector de opción única con el estilo de la aplicación.
 *
 * Reemplaza a <select>, cuya lista desplegable la dibuja el sistema operativo
 * y no se puede estilizar: por más clases que lleve el elemento, el panel de
 * opciones sale con la tipografía y colores nativos del navegador/SO. Mismo
 * motivo por el que existe app-date-picker en vez de <input type="date">.
 */
@Component({
  selector: 'app-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './select.component.html',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SelectComponent),
    multi: true
  }]
})
export class SelectComponent implements ControlValueAccessor, OnDestroy {

  /** Lista de objetos de origen; el valor y la etiqueta se leen por nombre de campo. */
  @Input() opciones: any[] = [];

  @Input() campoValor = 'value';
  @Input() campoEtiqueta = 'label';

  @Input() placeholder = 'Seleccione una opción';

  /**
   * Si se define, aparece como primera fila de la lista y permite volver a
   * null (p. ej. "Sin salón asignado"). Sin ella el placeholder es solo un
   * texto de estado vacío, no una opción elegible, igual que en un <select>
   * cuyo option null lleva "disabled".
   */
  @Input() etiquetaVacio: string | null = null;

  /** 'campo' = formulario en tarjeta/modal, 'drawer' = panel lateral, 'tabla' = celda compacta. */
  @Input() variante: 'campo' | 'drawer' | 'tabla' = 'campo';

  /** Color del borde/foco cuando variante es 'tabla'; distingue columnas (item vs. servicio). */
  @Input() acento: 'amber' | 'cyan' = 'amber';

  /** Pinta el borde en rojo; lo controla el formulario contenedor. */
  @Input() invalido = false;

  @Input() deshabilitado = false;

  @ViewChild('disparador') private disparadorRef!: ElementRef<HTMLButtonElement>;

  /**
   * variante 'tabla' vive dentro de un contenedor con overflow-x-auto (la
   * tabla de items/servicios): ese overflow recorta cualquier panel
   * `absolute` que se salga de sus bordes. Se saca el panel al <body> con
   * position:fixed calculado desde el trigger para escapar del recorte,
   * igual que resolvería un CDK Overlay pero sin traer la dependencia.
   */
  @ViewChild('panel') private set panelRef(ref: ElementRef<HTMLDivElement> | undefined) {
    if (ref && this.variante === 'tabla') {
      this.moverPanelAlBody(ref.nativeElement);
    }
  }

  abierto = false;
  valorActual: any = null;

  private alCambiar: (valor: any) => void = () => {};
  private alTocar: () => void = () => {};
  private readonly cerrarPorScroll = (): void => this.cerrar();

  constructor(private renderer: Renderer2) {}

  ngOnDestroy(): void {
    this.quitarListenerScroll();
  }

  // ─── ControlValueAccessor ──────────────────────────────────
  writeValue(valor: any): void {
    this.valorActual = valor ?? null;
  }

  registerOnChange(fn: (valor: any) => void): void { this.alCambiar = fn; }
  registerOnTouched(fn: () => void): void { this.alTocar = fn; }
  setDisabledState(deshabilitado: boolean): void { this.deshabilitado = deshabilitado; }

  // ─── Apertura ──────────────────────────────────────────────
  alternar(): void {
    if (this.deshabilitado) { return; }
    this.abierto = !this.abierto;
    if (this.abierto && this.variante === 'tabla') {
      // Captura, no burbujeo: el scroll dentro de la tabla no burbujea hasta
      // window, pero sí se puede escuchar en fase de captura.
      document.addEventListener('scroll', this.cerrarPorScroll, true);
      window.addEventListener('resize', this.cerrarPorScroll);
    } else {
      this.quitarListenerScroll();
    }
  }

  cerrar(): void {
    if (!this.abierto) { return; }
    this.abierto = false;
    this.alTocar();
    this.quitarListenerScroll();
  }

  private quitarListenerScroll(): void {
    document.removeEventListener('scroll', this.cerrarPorScroll, true);
    window.removeEventListener('resize', this.cerrarPorScroll);
  }

  private moverPanelAlBody(panel: HTMLElement): void {
    const rect = this.disparadorRef.nativeElement.getBoundingClientRect();
    this.renderer.setStyle(panel, 'position', 'fixed');
    this.renderer.setStyle(panel, 'top', `${rect.bottom + 8}px`);
    this.renderer.setStyle(panel, 'left', `${rect.left}px`);
    this.renderer.setStyle(panel, 'width', `${rect.width}px`);
    this.renderer.setStyle(panel, 'margin', '0');
    this.renderer.setStyle(panel, 'z-index', '9999');
    this.renderer.appendChild(document.body, panel);
  }

  // ─── Selección ─────────────────────────────────────────────
  seleccionar(opcion: any): void {
    this.valorActual = opcion[this.campoValor];
    this.alCambiar(this.valorActual);
    this.cerrar();
  }

  seleccionarVacio(): void {
    this.valorActual = null;
    this.alCambiar(null);
    this.cerrar();
  }

  esSeleccionado(opcion: any): boolean {
    return this.valorActual !== null && this.valorActual === opcion[this.campoValor];
  }

  get etiquetaSeleccionada(): string {
    const opcion = this.opciones.find(o => o[this.campoValor] === this.valorActual);
    return opcion ? opcion[this.campoEtiqueta] : '';
  }

  // ─── Clases (una por variante visual: campo, drawer o celda de tabla) ──
  get claseBase(): string {
    switch (this.variante) {
      case 'drawer':
        return 'w-full flex items-center justify-between gap-2 bg-white border-0 border-b pb-2 pt-3 sm:pt-0 text-[16px] sm:text-sm text-left transition-colors disabled:text-gray-400 disabled:cursor-not-allowed';
      case 'tabla':
        return 'w-full flex items-center justify-between gap-2 border-2 rounded-lg px-2 py-1.5 text-[16px] sm:text-sm text-left bg-white transition-all duration-150 disabled:bg-slate-50 disabled:text-gray-400 disabled:cursor-not-allowed';
      default:
        return 'w-full flex items-center justify-between gap-2 border-2 rounded-xl px-3 py-2.5 text-[16px] sm:text-sm text-left bg-white transition-all duration-150 disabled:bg-slate-50 disabled:text-gray-400 disabled:cursor-not-allowed';
    }
  }

  get claseBorde(): string {
    if (this.abierto) {
      return this.variante === 'tabla' && this.acento === 'cyan' ? 'border-cyan-500' : 'border-amber-400';
    }
    if (this.invalido) { return 'border-red-300'; }
    return this.variante === 'drawer' ? 'border-gray-200' : 'border-slate-200';
  }
}
