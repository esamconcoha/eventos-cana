import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ReporteService } from '../../services/reporte.service';
import { ToastService } from '../../shared/toast/toast.service';
import { ReporteGeneral, PresetPeriodo } from '../../interfaces/reporte';
import { PorcionDona } from '../../shared/graficos/grafico-dona.component';
import { FilaRanking } from '../../shared/graficos/grafico-ranking.component';
import { fechaISOLocal } from '../../shared/fecha.util';

interface OpcionPeriodo {
  clave: PresetPeriodo;
  etiqueta: string;
}

interface TarjetaIndicador {
  label: string;
  valor: string;
  nota: string;
  icono: string;
  chip: string;
  /** Pinta el número en ámbar: es un número que pide atención, no una alarma. */
  destacar?: boolean;
}

/**
 * Tablero de reportería. Muestra la foto del negocio en un periodo y la exporta
 * en PDF; el análisis libre (cruces, proyecciones) se llevará en Power BI sobre
 * la misma base, así que aquí no hay filtros por dimensión ni tablas dinámicas.
 */
@Component({
  selector: 'app-reportes',
  standalone: false,
  templateUrl: './reportes.component.html'
})
export class ReportesComponent implements OnInit {

  reporte: ReporteGeneral | null = null;
  cargando = false;
  generandoPdf = false;

  periodo: PresetPeriodo = 'anioMovil';
  desde = '';
  hasta = '';

  readonly opcionesPeriodo: OpcionPeriodo[] = [
    { clave: 'mesActual',   etiqueta: 'Este mes' },
    { clave: 'trimestre',   etiqueta: 'Últimos 3 meses' },
    { clave: 'semestre',    etiqueta: 'Últimos 6 meses' },
    { clave: 'anioMovil',   etiqueta: 'Últimos 12 meses' },
    { clave: 'anioActual',  etiqueta: 'Este año' },
    { clave: 'personalizado', etiqueta: 'Personalizado' }
  ];

  // Visor del PDF generado
  mostrarVisorDocumento = false;
  documentoBlob: Blob | null = null;
  documentoNombre = 'reporte-estadistico.pdf';

  constructor(
    private reporteService: ReporteService,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.aplicarPreset('anioMovil');
  }

  // ─── Periodo ─────────────────────────────────────────────
  aplicarPreset(preset: PresetPeriodo): void {
    this.periodo = preset;
    if (preset === 'personalizado') {
      // Se conserva el rango que ya estaba y se espera a que el usuario lo cambie.
      return;
    }

    const hoy = new Date();
    // Día 0 del mes siguiente = último día del mes actual (28/29/30/31 según
    // corresponda). "Últimos N meses" es un número exacto de meses completos,
    // no "hace N meses hasta hoy": si hoy es 3/08, "últimos 3 meses" es
    // 01/06–31/08, no 01/06–03/08. Solo "Este año" corta en el día de hoy,
    // porque ese preset sí es literalmente "lo que va del año".
    const finMes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    let hasta = finMes;
    let desde: Date;

    switch (preset) {
      case 'mesActual':
        desde = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
        break;
      case 'trimestre':
        desde = new Date(hoy.getFullYear(), hoy.getMonth() - 2, 1);
        break;
      case 'semestre':
        desde = new Date(hoy.getFullYear(), hoy.getMonth() - 5, 1);
        break;
      case 'anioActual':
        desde = new Date(hoy.getFullYear(), 0, 1);
        hasta = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
        break;
      case 'anioMovil':
      default:
        desde = new Date(hoy.getFullYear(), hoy.getMonth() - 11, 1);
        break;
    }

    this.desde = fechaISOLocal(desde);
    this.hasta = fechaISOLocal(hasta);
    this.cargar();
  }

  /** Los date-picker del modo personalizado no recargan solos: hay dos campos. */
  aplicarRangoPersonalizado(): void {
    if (!this.desde || !this.hasta) {
      this.toast.error('Rango incompleto', 'Selecciona la fecha de inicio y la de fin');
      return;
    }
    if (this.desde > this.hasta) {
      this.toast.error('Rango inválido', 'La fecha de inicio no puede ser posterior a la de fin');
      return;
    }
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.reporteService.obtenerGeneral(this.desde, this.hasta).subscribe({
      next: (data) => {
        this.reporte = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.cargando = false;
        this.toast.error('Error', err?.error?.message ?? 'No se pudo cargar el reporte');
        this.cdr.detectChanges();
      }
    });
  }

  // ─── Exportación ─────────────────────────────────────────
  exportarPdf(): void {
    this.generandoPdf = true;
    this.reporteService.descargarPdf(this.desde, this.hasta).subscribe({
      next: (response) => {
        this.generandoPdf = false;
        if (!response.body) {
          this.toast.error('Error', 'El reporte no se pudo generar');
          this.cdr.detectChanges();
          return;
        }
        this.documentoBlob = response.body;
        this.documentoNombre = `reporte-estadistico-${this.desde}-a-${this.hasta}.pdf`;
        this.mostrarVisorDocumento = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.generandoPdf = false;
        this.manejarErrorBlob(err, 'No se pudo generar el reporte en PDF');
        this.cdr.detectChanges();
      }
    });
  }

  cerrarVisorDocumento(): void {
    this.mostrarVisorDocumento = false;
    this.documentoBlob = null;
  }

  /** El endpoint responde blob, así que el error también llega como blob. */
  private manejarErrorBlob(err: any, mensajeDefault: string): void {
    const errorBlob: Blob | undefined = err?.error;
    if (errorBlob instanceof Blob && errorBlob.type.includes('json')) {
      errorBlob.text().then(texto => {
        let mensaje = mensajeDefault;
        try { mensaje = JSON.parse(texto)?.message ?? mensaje; } catch { /* respuesta no JSON */ }
        this.toast.error('Error', mensaje);
        this.cdr.detectChanges();
      });
      return;
    }
    this.toast.error('Error', err?.error?.message ?? mensajeDefault);
  }

  // ─── Datos derivados para la vista ───────────────────────
  get indicadores(): TarjetaIndicador[] {
    if (!this.reporte) { return []; }
    const r = this.reporte.resumen;
    return [
      {
        label: 'Eventos del periodo', valor: this.entero(r.eventos),
        nota: `${r.eventosFinalizados} finalizados · ${r.eventosCancelados} cancelados`,
        icono: 'celebration', chip: 'bg-purple-100 text-purple-700'
      },
      {
        label: 'Facturado', valor: this.moneda(r.facturado),
        nota: 'Artículos y servicios de los pedidos',
        icono: 'payments', chip: 'bg-slate-100 text-slate-600'
      },
      {
        label: 'Cobrado', valor: this.moneda(r.cobrado),
        nota: `${this.porcentaje(r.porcentajeCobrado)} de lo facturado`,
        icono: 'account_balance_wallet', chip: 'bg-emerald-100 text-emerald-700'
      },
      {
        label: 'Saldo por cobrar', valor: this.moneda(r.saldoPorCobrar),
        nota: `${r.pedidosConSaldo} pedidos con saldo`,
        icono: 'request_quote', chip: 'bg-amber-100 text-amber-700',
        destacar: r.saldoPorCobrar > 0
      },
      {
        label: 'Ticket promedio', valor: this.moneda(r.ticketPromedio),
        nota: 'Por evento no cancelado',
        icono: 'trending_up', chip: 'bg-sky-100 text-sky-700'
      },
      {
        label: 'Conversión de cotizaciones', valor: this.porcentaje(r.tasaConversion),
        nota: `${r.cotizacionesConfirmadas} de ${r.cotizaciones} cotizaciones`,
        icono: 'swap_horiz', chip: 'bg-slate-100 text-slate-600'
      },
      {
        label: 'Tasa de cancelación', valor: this.porcentaje(r.tasaCancelacion),
        nota: `${r.eventosCancelados} eventos cancelados`,
        icono: 'event_busy', chip: 'bg-red-100 text-red-600',
        destacar: r.tasaCancelacion > 0
      },
      {
        label: 'Faltantes en bodega', valor: this.entero(r.unidadesFaltantes),
        nota: `${r.articulosConFaltantes} artículos afectados`,
        icono: 'report_problem', chip: 'bg-red-100 text-red-600',
        destacar: r.unidadesFaltantes > 0
      }
    ];
  }

  get etiquetasMeses(): string[] {
    return this.reporte?.serieMensual.map(m => m.etiqueta) ?? [];
  }

  get serieFacturado(): number[] {
    return this.reporte?.serieMensual.map(m => m.facturado) ?? [];
  }

  get serieCobrado(): number[] {
    return this.reporte?.serieMensual.map(m => m.cobrado) ?? [];
  }

  get serieEventos(): number[] {
    return this.reporte?.serieMensual.map(m => m.eventos) ?? [];
  }

  get donaTipos(): PorcionDona[] {
    return (this.reporte?.eventosPorTipo ?? []).map(d => ({
      etiqueta: d.etiqueta,
      valor: d.cantidad,
      detalle: `${d.cantidad} eventos · ${this.moneda(d.monto)}`
    }));
  }

  /** Reparte dinero, no cantidad: la pregunta es cuánto se debe, no cuántos deben. */
  get donaCartera(): PorcionDona[] {
    return (this.reporte?.carteraPorEstadoPago ?? []).map(d => ({
      etiqueta: d.etiqueta,
      valor: d.monto,
      detalle: `${d.cantidad} pedidos · ${this.moneda(d.monto)}`
    }));
  }

  get rankingArticulos(): FilaRanking[] {
    return (this.reporte?.topArticulos ?? []).map(a => ({
      etiqueta: a.etiqueta,
      detalle: a.categoria,
      valor: a.cantidad,
      valorTexto: `${this.numero(a.cantidad)} u`
    }));
  }

  get rankingServicios(): FilaRanking[] {
    return (this.reporte?.topServicios ?? []).map(s => ({
      etiqueta: s.etiqueta,
      detalle: s.categoria,
      valor: s.monto,
      valorTexto: this.moneda(s.monto)
    }));
  }

  get rankingEstados(): FilaRanking[] {
    return (this.reporte?.eventosPorEstado ?? []).map(e => ({
      etiqueta: e.etiqueta,
      detalle: `${this.porcentaje(e.porcentaje)} de los eventos`,
      valor: e.cantidad,
      valorTexto: String(e.cantidad)
    }));
  }

  // ─── Formato ─────────────────────────────────────────────
  moneda(valor: number | null | undefined): string {
    return 'Q ' + (valor ?? 0).toLocaleString('es-GT', {
      minimumFractionDigits: 2, maximumFractionDigits: 2
    });
  }

  numero(valor: number | null | undefined): string {
    return (valor ?? 0).toLocaleString('es-GT', { maximumFractionDigits: 2 });
  }

  entero(valor: number | null | undefined): string {
    return (valor ?? 0).toLocaleString('es-GT', { maximumFractionDigits: 0 });
  }

  porcentaje(valor: number | null | undefined): string {
    return (valor ?? 0).toFixed(1) + '%';
  }

  fechaCorta(fechaIso: string | null | undefined): string {
    if (!fechaIso) { return ''; }
    const [y, m, d] = fechaIso.slice(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }
}
