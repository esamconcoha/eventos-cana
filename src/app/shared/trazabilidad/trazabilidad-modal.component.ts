import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  TrazaEvento, TrazaEstadoDef, TrazaFlujo,
  FLUJO_PEDIDO, FLUJO_COTIZACION,
  defEstado, esEstadoFueraDeFlujo, avanceHistorial,
  duracionMs, duracionTotalMs, formatearDuracion
} from '../../interfaces/trazabilidad';

/** Fila ya resuelta de la línea de tiempo (no se calcula en el template). */
interface FilaTraza {
  label: string;
  dot: string;
  badgeClass: string;
  fechaInicio: Date;
  fechaFin: Date | null;
  duracion: string;
  esActual: boolean;
  fueraDeFlujo: boolean;
}

/** Un paso del stepper con su situación respecto al avance actual. */
interface PasoStepper {
  def: TrazaEstadoDef;
  alcanzado: boolean;
  esActual: boolean;
}

/**
 * Ventana de trazabilidad, compartida por pedidos y cotizaciones.
 *
 * Recibe el historial ya cargado en vez de pedirlo: los dos módulos usan
 * servicios distintos (PedidoService / CotizacionService) y meterlos acá
 * ataría un componente de presentación a las dos capas de datos.
 *
 * Todo lo derivado se calcula en ngOnChanges y NO en getters del template:
 * las duraciones dependen de Date.now(), y resolverlas en cada ciclo de
 * detección de cambios daría un valor distinto cada vez.
 */
@Component({
  selector: 'app-trazabilidad-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './trazabilidad-modal.component.html'
})
export class TrazabilidadModalComponent implements OnChanges {
  @Input() visible = false;
  @Input() tipo: 'PEDIDO' | 'COTIZACION' = 'PEDIDO';
  /** Identificador visible: correlativo del pedido o código de la cotización. */
  @Input() titulo = '';
  /** Segunda línea del encabezado: normalmente el nombre del cliente. */
  @Input() subtitulo = '';
  @Input() historial: TrazaEvento[] = [];
  @Input() cargando = false;
  @Output() cerrarModal = new EventEmitter<void>();

  flujo: TrazaFlujo = FLUJO_PEDIDO;
  filas: FilaTraza[] = [];
  pasos: PasoStepper[] = [];

  porcentaje = 0;
  estadoActual: TrazaEstadoDef | undefined;
  nombreEstadoActual = '—';
  detenido = false;            // cancelada / eliminada: el avance quedó congelado

  duracionTotal = '—';
  duracionEstadoActual = '—';
  totalCambios = 0;
  fechaInicioCiclo: Date | null = null;

  // Anillo de progreso (r = 34 → circunferencia = 2πr).
  readonly circunferencia = 2 * Math.PI * 34;

  get offsetAnillo(): number {
    return this.circunferencia * (1 - this.porcentaje / 100);
  }

  get colorAnillo(): string {
    if (this.detenido) { return '#f87171'; }        // red-400
    if (this.porcentaje >= 100) { return '#34d399'; } // emerald-400
    return '#fbbf24';                                // amber-400
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['historial'] || changes['tipo'] || changes['visible']) {
      this.flujo = this.tipo === 'COTIZACION' ? FLUJO_COTIZACION : FLUJO_PEDIDO;
      this.recalcular();
    }
  }

  private recalcular(): void {
    const historial = this.historial ?? [];
    this.totalCambios = historial.length;

    // El backend devuelve el historial del más reciente al más antiguo, y así
    // se muestra: lo último que pasó es lo que se busca al abrir la ventana.
    this.filas = historial.map(ev => {
      const def = defEstado(this.flujo, ev.codigoEstado);
      return {
        label: def?.label ?? ev.nombreEstado ?? ev.codigoEstado ?? '—',
        dot: def?.dot ?? 'bg-slate-300',
        badgeClass: def?.badgeClass ?? 'bg-slate-50 text-slate-600 ring-slate-200',
        fechaInicio: new Date(ev.fechaHoraInicio),
        fechaFin: ev.fechaHoraFin ? new Date(ev.fechaHoraFin) : null,
        duracion: formatearDuracion(duracionMs(ev)),
        esActual: !ev.fechaHoraFin,
        fueraDeFlujo: esEstadoFueraDeFlujo(this.flujo, ev.codigoEstado)
      };
    });

    // El estado actual es el tramo abierto (fecha_hora_fin null). Si por algún
    // motivo no hubiera ninguno abierto, se cae al más reciente por fecha.
    const abierto = historial.find(e => !e.fechaHoraFin) ?? historial[0];
    this.estadoActual = defEstado(this.flujo, abierto?.codigoEstado);
    this.nombreEstadoActual =
      this.estadoActual?.label ?? abierto?.nombreEstado ?? '—';
    this.detenido = esEstadoFueraDeFlujo(this.flujo, abierto?.codigoEstado);

    // Cancelada: el avance no baja a 0, queda congelado en lo que alcanzó.
    this.porcentaje = avanceHistorial(this.flujo, historial);

    this.duracionEstadoActual = abierto ? formatearDuracion(duracionMs(abierto)) : '—';
    this.duracionTotal = historial.length ? formatearDuracion(duracionTotalMs(historial)) : '—';

    this.fechaInicioCiclo = historial.length
      ? new Date(Math.min(...historial.map(e => new Date(e.fechaHoraInicio).getTime())))
      : null;

    const codigosVividos = new Set(historial.map(e => e.codigoEstado));
    this.pasos = this.flujo.pasos.map(def => ({
      def,
      alcanzado: codigosVividos.has(def.codigo),
      esActual: def.codigo === this.estadoActual?.codigo
    }));
  }

  cerrar(): void {
    this.cerrarModal.emit();
  }
}
