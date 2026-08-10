import { Component, Input, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/**
 * Selector de fecha (y hora opcional) con el estilo de la aplicación.
 *
 * Reemplaza a <input type="date"> / <input type="datetime-local">, cuyo
 * calendario lo dibuja el navegador y no se puede estilizar: cada navegador lo
 * mostraba distinto y ninguno se parecía al resto de la interfaz.
 *
 * Emite SIEMPRE texto en hora local, nunca toISOString():
 *   modo 'fecha'      -> "2026-07-21"
 *   modo 'fechaHora'  -> "2026-07-21T19:30:00"
 * toISOString() convierte a UTC y en UTC-6 corre la fecha seis horas, así que
 * un evento de las 20:00 se guardaba al día siguiente.
 */
@Component({
  selector: 'app-date-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './date-picker.component.html',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => DatePickerComponent),
    multi: true
  }]
})
export class DatePickerComponent implements ControlValueAccessor {

  /** 'fecha' muestra solo el calendario; 'fechaHora' agrega selector de hora. */
  @Input() modo: 'fecha' | 'fechaHora' = 'fecha';

  /** Bloquea los días anteriores a hoy. Para agendar a futuro. */
  @Input() minimoHoy = false;

  /**
   * Límites de rango, como "yyyy-mm-dd". Inhabilitan los días fuera de rango
   * igual que minimoHoy. Se usan para encadenar fechas dependientes: la entrega
   * no puede pasar del evento, la recolección no puede ser antes de la entrega.
   * Null = sin límite por ese lado.
   */
  @Input() fechaMinima: string | null = null;
  @Input() fechaMaxima: string | null = null;

  @Input() placeholder = 'Selecciona una fecha';

  /** Pinta el borde en rojo; lo controla el formulario contenedor. */
  @Input() invalido = false;

  @Input() deshabilitado = false;

  abierto = false;
  horaAbierta = false;
  mesVisible = new Date();
  seleccionada: Date | null = null;
  hora = '';

  readonly nombresDias = ['D', 'L', 'M', 'M', 'J', 'V', 'S'];
  readonly horasDisponibles = Array.from({ length: 24 }, (_, i) => i);
  readonly minutosDisponibles = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

  private alCambiar: (valor: string | null) => void = () => {};
  private alTocar: () => void = () => {};

  // ─── ControlValueAccessor ──────────────────────────────────
  writeValue(valor: string | null): void {
    if (!valor) {
      this.seleccionada = null;
      this.hora = '';
      return;
    }
    // Se parsea por partes, no con new Date(texto): "2026-07-21" se
    // interpretaría como UTC medianoche y mostraría el día anterior.
    const [fechaTexto, horaTexto] = valor.split('T');
    const [y, m, d] = fechaTexto.split('-').map(Number);
    if (!y || !m || !d) { return; }

    this.seleccionada = new Date(y, m - 1, d);
    this.mesVisible = new Date(y, m - 1, 1);
    this.hora = horaTexto ? horaTexto.slice(0, 5) : '';
  }

  registerOnChange(fn: (valor: string | null) => void): void { this.alCambiar = fn; }
  registerOnTouched(fn: () => void): void { this.alTocar = fn; }
  setDisabledState(deshabilitado: boolean): void { this.deshabilitado = deshabilitado; }

  // ─── Apertura ──────────────────────────────────────────────
  abrir(): void {
    if (this.deshabilitado) { return; }
    if (this.seleccionada) {
      this.mesVisible = new Date(this.seleccionada.getFullYear(), this.seleccionada.getMonth(), 1);
    }
    this.abierto = true;
  }

  cerrar(): void {
    this.abierto = false;
    this.horaAbierta = false;
    this.alTocar();
  }

  // ─── Grilla del mes ────────────────────────────────────────
  get dias(): (Date | null)[] {
    const y = this.mesVisible.getFullYear();
    const m = this.mesVisible.getMonth();
    const primero = new Date(y, m, 1);
    const ultimo = new Date(y, m + 1, 0);

    const dias: (Date | null)[] = [];
    for (let i = 0; i < primero.getDay(); i++) { dias.push(null); }
    for (let d = 1; d <= ultimo.getDate(); d++) { dias.push(new Date(y, m, d)); }
    return dias;
  }

  get nombreMes(): string {
    const texto = this.mesVisible.toLocaleDateString('es-GT', { month: 'long', year: 'numeric' });
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  }

  mesAnterior(): void {
    this.mesVisible = new Date(this.mesVisible.getFullYear(), this.mesVisible.getMonth() - 1, 1);
  }

  mesSiguiente(): void {
    this.mesVisible = new Date(this.mesVisible.getFullYear(), this.mesVisible.getMonth() + 1, 1);
  }

  esHoy(dia: Date): boolean {
    return dia.toDateString() === new Date().toDateString();
  }

  esSeleccionado(dia: Date): boolean {
    return !!this.seleccionada && dia.toDateString() === this.seleccionada.toDateString();
  }

  /**
   * Un día queda inhabilitado si cae antes de hoy (minimoHoy) o fuera del rango
   * [fechaMinima, fechaMaxima]. La comparación es a día, sin hora.
   */
  esBloqueado(dia: Date): boolean {
    const d = this.aMedianoche(dia);

    if (this.minimoHoy && d < this.aMedianoche(new Date())) { return true; }

    const min = this.parseLimite(this.fechaMinima);
    if (min && d < min) { return true; }

    const max = this.parseLimite(this.fechaMaxima);
    if (max && d > max) { return true; }

    return false;
  }

  private aMedianoche(fecha: Date): Date {
    return new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
  }

  /** "yyyy-mm-dd" → Date local a medianoche. Ignora cualquier parte de hora. */
  private parseLimite(valor: string | null): Date | null {
    if (!valor) { return null; }
    const [y, m, d] = valor.slice(0, 10).split('-').map(Number);
    return y && m && d ? new Date(y, m - 1, d) : null;
  }

  // ─── Selección ─────────────────────────────────────────────
  seleccionarDia(dia: Date): void {
    if (this.esBloqueado(dia)) { return; }
    this.seleccionada = dia;
    if (this.modo === 'fechaHora' && !this.hora) {
      this.hora = '08:00';
    }
    this.emitir();
    if (this.modo === 'fecha') { this.cerrar(); }
  }

  toggleHora(event: Event): void {
    event.stopPropagation();
    this.horaAbierta = !this.horaAbierta;
  }

  get horaNum(): number | null {
    return this.hora ? Number(this.hora.split(':')[0]) : null;
  }

  get minutoNum(): number | null {
    return this.hora ? Number(this.hora.split(':')[1]) : null;
  }

  seleccionarHora(h: number): void {
    this.hora = `${this.dosDigitos(h)}:${this.dosDigitos(this.minutoNum ?? 0)}`;
    this.emitir();
  }

  seleccionarMinuto(m: number): void {
    this.hora = `${this.dosDigitos(this.horaNum ?? 0)}:${this.dosDigitos(m)}`;
    this.emitir();
  }

  limpiar(event: Event): void {
    event.stopPropagation();
    this.seleccionada = null;
    this.hora = '';
    this.alCambiar(null);
    this.cerrar();
  }

  // ─── Salida ────────────────────────────────────────────────
  get texto(): string {
    if (!this.seleccionada) { return ''; }
    const fecha = this.seleccionada.toLocaleDateString('es-GT', {
      day: 'numeric', month: 'long', year: 'numeric'
    });
    return this.modo === 'fechaHora' && this.hora ? `${fecha}, ${this.hora}` : fecha;
  }

  private emitir(): void {
    if (!this.seleccionada) { this.alCambiar(null); return; }

    const y = this.seleccionada.getFullYear();
    const m = this.dosDigitos(this.seleccionada.getMonth() + 1);
    const d = this.dosDigitos(this.seleccionada.getDate());
    const fecha = `${y}-${m}-${d}`;

    this.alCambiar(this.modo === 'fechaHora' ? `${fecha}T${this.hora || '00:00'}:00` : fecha);
  }

  private dosDigitos(n: number): string {
    return String(n).padStart(2, '0');
  }
}
