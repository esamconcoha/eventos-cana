import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { RecoleccionService } from '../../../services/recoleccion.service';
import { categoriaFechaEntrega } from '../../../services/entrega.service';
import { EntregaPedido, EstadisticasEntregas, EstadoEntregaDerivado } from '../../../interfaces/entrega';
import { EstadoLogisticoDef, estadoLogisticoPorCodigo } from '../../../interfaces/estado-logistico';

interface CategoriaFechaDef {
  codigo: EstadoEntregaDerivado;
  label: string;
  dot: string;
  badgeClass: string;
}

type TabRecolecciones = 'todas' | 'sin-fecha' | 'hoy' | 'programadas' | 'atrasadas' | 'finalizadas';

@Component({
  selector: 'app-recolecciones',
  standalone: false,
  templateUrl: './recolecciones.component.html',
  styleUrl: './recolecciones.component.css'
})
export class RecoleccionesComponent implements OnInit {

  // Mismo eje de agenda que en Entregas, pero medido sobre la fecha programada
  // de recolección (el backend ya devuelve esa fecha en fechaEntrega).
  categorias: CategoriaFechaDef[] = [
    { codigo: 'SIN_FECHA',  label: 'Sin agendar', dot: 'bg-slate-400',   badgeClass: 'bg-slate-50 text-slate-600 ring-slate-200' },
    { codigo: 'ATRASADA',   label: 'Atrasada',    dot: 'bg-red-500',     badgeClass: 'bg-red-50 text-red-600 ring-red-200' },
    { codigo: 'HOY',        label: 'Hoy',         dot: 'bg-amber-400',   badgeClass: 'bg-amber-50 text-amber-700 ring-amber-200' },
    { codigo: 'PROGRAMADA', label: 'Programada',  dot: 'bg-sky-400',     badgeClass: 'bg-sky-50 text-sky-700 ring-sky-200' },
    { codigo: 'FINALIZADA', label: 'Finalizada',  dot: 'bg-emerald-500', badgeClass: 'bg-emerald-50 text-emerald-700 ring-emerald-200' }
  ];

  recolecciones: EntregaPedido[] = [];
  recoleccionesFiltradas: EntregaPedido[] = [];
  estadisticas: EstadisticasEntregas | null = null;
  cargando = false;
  tabActiva: TabRecolecciones = 'todas';

  constructor(
    private recoleccionService: RecoleccionService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargar();
    this.cargarEstadisticas();
  }

  cargar(): void {
    this.cargando = true;
    this.recoleccionService.listarRecolecciones().subscribe({
      next: (data) => {
        this.recolecciones = data;
        this.aplicarFiltro();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  cargarEstadisticas(): void {
    this.recoleccionService.obtenerEstadisticas().subscribe({
      next: (data) => { this.estadisticas = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  // ─── Categoría por fecha programada (eje de agenda) ────────
  categoriaFecha(r: EntregaPedido): EstadoEntregaDerivado {
    return categoriaFechaEntrega(r);
  }

  categoriaDef(r: EntregaPedido): CategoriaFechaDef {
    return this.categorias.find(c => c.codigo === this.categoriaFecha(r))!;
  }

  // ─── Estado logístico real del pedido ──────────────────────
  estadoPedidoDef(r: EntregaPedido): EstadoLogisticoDef | undefined {
    return estadoLogisticoPorCodigo(r.estadoActualPedido?.codigoEstado);
  }

  estadoPedidoLabel(r: EntregaPedido): string {
    return r.estadoActualPedido?.nombreEstado ?? this.estadoPedidoDef(r)?.label ?? '—';
  }

  // ─── Tabs ──────────────────────────────────────────────────
  cambiarTab(tab: TabRecolecciones): void {
    this.tabActiva = tab;
    this.aplicarFiltro();
  }

  aplicarFiltro(): void {
    this.recoleccionesFiltradas = this.recolecciones.filter(r => {
      const cat = this.categoriaFecha(r);
      switch (this.tabActiva) {
        case 'sin-fecha':   return cat === 'SIN_FECHA';
        case 'hoy':         return cat === 'HOY';
        case 'programadas': return cat === 'PROGRAMADA';
        case 'atrasadas':   return cat === 'ATRASADA';
        case 'finalizadas': return cat === 'FINALIZADA';
        default:            return true;
      }
    });
  }

  // ─── Avance ────────────────────────────────────────────────
  avancePct(r: EntregaPedido): number {
    if (!r.cantidadViajesAproximados) { return 0; }
    return Math.min(r.cantidadViajesReales / r.cantidadViajesAproximados, 1) * 100;
  }

  avanceColorClass(r: EntregaPedido): string {
    if (r.cantidadViajesReales > r.cantidadViajesAproximados) { return 'bg-red-500'; }
    if (r.pedidoFinalizado) { return 'bg-emerald-500'; }
    return 'bg-sky-400';
  }

  verRecoleccion(r: EntregaPedido): void {
    this.router.navigate(['/gestion-interna/eventos/recolecciones', r.idEntrega]);
  }
}
