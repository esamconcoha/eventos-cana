import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CalendarioService } from '../../../services/calendario.service';
import {
  CalendarioItem, TipoCalendario, TipoCalendarioDef,
  TIPOS_CALENDARIO, tipoCalendarioDef
} from '../../../interfaces/calendario';
import { estadoLogisticoPorCodigo } from '../../../interfaces/estado-logistico';
import { fechaISOLocal, hoyISOLocal } from '../../../shared/fecha.util';

interface DiaCalendario {
  fecha: string;       // "yyyy-mm-dd", misma clave que CalendarioItem.fecha
  numero: number;
  esDelMes: boolean;   // los días de relleno del mes anterior/siguiente van atenuados
  esHoy: boolean;
  items: CalendarioItem[];
}

@Component({
  selector: 'app-calendario',
  standalone: false,
  templateUrl: './calendario.component.html',
  styleUrl: './calendario.component.css'
})
export class CalendarioComponent implements OnInit {

  readonly nombresDias = ['DOM', 'LUN', 'MAR', 'MIÉ', 'JUE', 'VIE', 'SÁB'];
  readonly tipos: TipoCalendarioDef[] = TIPOS_CALENDARIO;

  /** Día 1 del mes visible. Toda la navegación se mueve sobre esta fecha. */
  mesVisible!: Date;
  dias: DiaCalendario[] = [];
  diaSeleccionado: DiaCalendario | null = null;
  cargando = false;

  /** Filtro de la leyenda: los chips de la leyenda encienden y apagan tipos. */
  tiposActivos = new Set<TipoCalendario>(TIPOS_CALENDARIO.map(t => t.tipo));

  private agenda: CalendarioItem[] = [];

  constructor(
    private calendarioService: CalendarioService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const hoy = new Date();
    this.mesVisible = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    this.cargarMes();
  }

  // ─── Navegación ────────────────────────────────────────────
  mesAnterior(): void {
    this.mesVisible = new Date(this.mesVisible.getFullYear(), this.mesVisible.getMonth() - 1, 1);
    this.cargarMes();
  }

  mesSiguiente(): void {
    this.mesVisible = new Date(this.mesVisible.getFullYear(), this.mesVisible.getMonth() + 1, 1);
    this.cargarMes();
  }

  irAHoy(): void {
    const hoy = new Date();
    this.mesVisible = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    this.cargarMes(hoyISOLocal());
  }

  // ─── Carga ─────────────────────────────────────────────────
  /**
   * La grilla siempre muestra semanas completas, así que el rango pedido va
   * del domingo anterior al día 1 hasta el sábado posterior al último día:
   * incluye días de los meses vecinos que igual se ven en pantalla.
   */
  private cargarMes(fechaAseleccionar?: string): void {
    const { inicio, fin } = this.rangoVisible();
    this.cargando = true;

    this.calendarioService.obtenerAgenda(fechaISOLocal(inicio), fechaISOLocal(fin)).subscribe({
      next: (data) => {
        this.agenda = data;
        this.construirGrilla(inicio, fin);
        this.seleccionarPorFecha(fechaAseleccionar ?? this.fechaPorDefecto());
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.agenda = [];
        this.construirGrilla(inicio, fin);
        this.diaSeleccionado = null;
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  private rangoVisible(): { inicio: Date; fin: Date } {
    const y = this.mesVisible.getFullYear();
    const m = this.mesVisible.getMonth();
    const primero = new Date(y, m, 1);
    const ultimo = new Date(y, m + 1, 0);
    return {
      inicio: new Date(y, m, 1 - primero.getDay()),
      fin: new Date(y, m, ultimo.getDate() + (6 - ultimo.getDay()))
    };
  }

  private construirGrilla(inicio: Date, fin: Date): void {
    const porFecha = new Map<string, CalendarioItem[]>();
    for (const item of this.agenda) {
      if (!item.fecha) { continue; }
      const clave = item.fecha.slice(0, 10);
      if (!porFecha.has(clave)) { porFecha.set(clave, []); }
      porFecha.get(clave)!.push(item);
    }

    const hoy = hoyISOLocal();
    const mes = this.mesVisible.getMonth();
    const dias: DiaCalendario[] = [];

    for (let d = new Date(inicio); d <= fin; d.setDate(d.getDate() + 1)) {
      const clave = fechaISOLocal(d);
      dias.push({
        fecha: clave,
        numero: d.getDate(),
        esDelMes: d.getMonth() === mes,
        esHoy: clave === hoy,
        items: porFecha.get(clave) ?? []
      });
    }
    this.dias = dias;
  }

  /** Al cambiar de mes se abre el día con actividad más próximo, no uno vacío. */
  private fechaPorDefecto(): string | undefined {
    const hoy = hoyISOLocal();
    if (this.dias.some(d => d.fecha === hoy && d.esDelMes)) { return hoy; }
    return this.dias.find(d => d.esDelMes && this.itemsVisibles(d).length > 0)?.fecha;
  }

  private seleccionarPorFecha(fecha?: string): void {
    this.diaSeleccionado = fecha ? this.dias.find(d => d.fecha === fecha) ?? null : null;
  }

  // ─── Interacción ───────────────────────────────────────────
  seleccionarDia(dia: DiaCalendario): void {
    this.diaSeleccionado = dia;
  }

  alternarTipo(tipo: TipoCalendario): void {
    if (this.tiposActivos.has(tipo)) {
      this.tiposActivos.delete(tipo);
    } else {
      this.tiposActivos.add(tipo);
    }
    // Set mutado: se reasigna para que Angular note el cambio
    this.tiposActivos = new Set(this.tiposActivos);
  }

  tipoActivo(tipo: TipoCalendario): boolean {
    return this.tiposActivos.has(tipo);
  }

  itemsVisibles(dia: DiaCalendario): CalendarioItem[] {
    return dia.items.filter(i => this.tiposActivos.has(i.tipo));
  }

  /** Navega al módulo que corresponde según el tipo de entrada. */
  abrirItem(item: CalendarioItem): void {
    if (item.tipo === 'ENTREGA' && item.idEntrega != null) {
      this.router.navigate(['/gestion-interna/eventos/entregas', item.idEntrega]);
    } else {
      this.router.navigate(['/gestion-interna/eventos/pedidos']);
    }
  }

  // ─── Presentación ──────────────────────────────────────────
  defTipo(item: CalendarioItem): TipoCalendarioDef {
    return tipoCalendarioDef(item.tipo);
  }

  /** "19:00", o vacío si el origen del dato no tiene hora (ENTREGA). */
  hora(item: CalendarioItem): string {
    if (!item.fechaHora) { return ''; }
    return item.fechaHora.slice(11, 16);
  }

  estadoDot(item: CalendarioItem): string {
    return estadoLogisticoPorCodigo(item.codigoEstadoPedido)?.dot ?? 'bg-slate-300';
  }

  get tituloMes(): string {
    const texto = this.mesVisible.toLocaleDateString('es', { month: 'long', year: 'numeric' });
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  }

  get subtituloDiaSeleccionado(): string {
    if (!this.diaSeleccionado) { return ''; }
    // Se construye la fecha con partes locales: new Date("yyyy-mm-dd") la
    // interpretaría como UTC medianoche y en UTC-6 mostraría el día anterior.
    const [y, m, d] = this.diaSeleccionado.fecha.split('-').map(Number);
    return new Date(y, m - 1, d).toLocaleDateString('es', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    });
  }

  get totalVisiblesDelMes(): number {
    return this.dias.filter(d => d.esDelMes).reduce((acc, d) => acc + this.itemsVisibles(d).length, 0);
  }
}
