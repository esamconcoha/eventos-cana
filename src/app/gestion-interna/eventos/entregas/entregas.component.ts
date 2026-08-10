import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { EntregaService, categoriaFechaEntrega } from '../../../services/entrega.service';
import { EntregaPedido, EstadisticasEntregas, EstadoEntregaDerivado } from '../../../interfaces/entrega';
import { EstadoLogisticoDef, estadoLogisticoPorCodigo } from '../../../interfaces/estado-logistico';

interface EstadoEntregaDef {
  codigo: EstadoEntregaDerivado;
  label: string;
  dot: string;
  badgeClass: string;
}

type TabEntregas = 'todas' | 'hoy' | 'programadas' | 'atrasadas' | 'sin-fecha' | 'finalizadas';

@Component({
  selector: 'app-entregas',
  standalone: false,
  templateUrl: './entregas.component.html',
  styleUrl: './entregas.component.css'
})
export class EntregasComponent implements OnInit {

  // Categoría por fecha PACTADA (pedidos_cana.fecha_entrega) vs. hoy, no por
  // si ya se registró un viaje — ver categoriaFechaEntrega() en el servicio.
  estados: EstadoEntregaDef[] = [
    { codigo: 'ATRASADA',   label: 'Atrasada',   dot: 'bg-red-500',     badgeClass: 'bg-red-50 text-red-600 ring-red-200' },
    { codigo: 'HOY',        label: 'Hoy',        dot: 'bg-amber-400',   badgeClass: 'bg-amber-50 text-amber-700 ring-amber-200' },
    { codigo: 'PROGRAMADA', label: 'Programada', dot: 'bg-sky-400',     badgeClass: 'bg-sky-50 text-sky-700 ring-sky-200' },
    { codigo: 'SIN_FECHA',  label: 'Sin fecha',  dot: 'bg-slate-300',   badgeClass: 'bg-slate-50 text-slate-500 ring-slate-200' },
    { codigo: 'FINALIZADA', label: 'Finalizada', dot: 'bg-emerald-500', badgeClass: 'bg-emerald-50 text-emerald-700 ring-emerald-200' }
  ];

  entregas: EntregaPedido[] = [];
  entregasFiltradas: EntregaPedido[] = [];
  estadisticas: EstadisticasEntregas | null = null;
  cargando = false;

  tabActiva: TabEntregas = 'todas';

  constructor(
    private entregaService: EntregaService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarEntregas();
    this.cargarEstadisticas();
  }

  cargarEntregas(): void {
    this.cargando = true;
    this.entregaService.listarEntregas().subscribe({
      next: (data) => {
        this.entregas = data;
        this.aplicarFiltro();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  cargarEstadisticas(): void {
    this.entregaService.obtenerEstadisticas().subscribe({
      next: (data) => { this.estadisticas = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  // ─── Categoría por fecha ───────────────────────────────────
  // Eje de AGENDA (¿para cuándo es?), no de estado. Alimenta las tabs, los
  // contadores del tablero y el color de la fecha. No se muestra como "Estado"
  // en la tabla: ahí va el estado real del pedido, ver estadoPedidoLabel().
  estadoDerivado(entrega: EntregaPedido): EstadoEntregaDerivado {
    return categoriaFechaEntrega(entrega);
  }

  estadoDef(entrega: EntregaPedido): EstadoEntregaDef {
    return this.estados.find(e => e.codigo === this.estadoDerivado(entrega))!;
  }

  // ─── Estado logístico real del pedido ──────────────────────
  // El mismo que muestra la pantalla de Pedidos, con la misma etiqueta y el
  // mismo color: el operador no puede ver dos nombres para el mismo pedido.
  // La etiqueta sale del backend (cana.estados.nombre_estado); el catálogo
  // compartido solo aporta el color.
  estadoPedidoDef(entrega: EntregaPedido): EstadoLogisticoDef | undefined {
    return estadoLogisticoPorCodigo(entrega.estadoActualPedido?.codigoEstado);
  }

  estadoPedidoLabel(entrega: EntregaPedido): string {
    return entrega.estadoActualPedido?.nombreEstado
        ?? this.estadoPedidoDef(entrega)?.label
        ?? '—';
  }

  // ─── Tabs ──────────────────────────────────────────────────
  cambiarTab(tab: TabEntregas): void {
    this.tabActiva = tab;
    this.aplicarFiltro();
  }

  aplicarFiltro(): void {
    this.entregasFiltradas = this.entregas.filter(e => {
      const estado = this.estadoDerivado(e);
      switch (this.tabActiva) {
        case 'hoy':         return estado === 'HOY';
        case 'programadas': return estado === 'PROGRAMADA';
        case 'atrasadas':   return estado === 'ATRASADA';
        case 'sin-fecha':   return estado === 'SIN_FECHA';
        case 'finalizadas': return estado === 'FINALIZADA';
        default:            return true;
      }
    });
  }

  // ─── Avance (viajes reales vs. aproximados) ───────────────────
  avancePct(entrega: EntregaPedido): number {
    if (!entrega.cantidadViajesAproximados) return 0;
    return Math.min(entrega.cantidadViajesReales / entrega.cantidadViajesAproximados, 1) * 100;
  }

  avanceColorClass(entrega: EntregaPedido): string {
    if (entrega.cantidadViajesReales > entrega.cantidadViajesAproximados) return 'bg-red-500';
    if (entrega.pedidoFinalizado) return 'bg-emerald-500';
    return 'bg-amber-400';
  }

  // ─── Navegación ────────────────────────────────────────────
  verEntrega(entrega: EntregaPedido): void {
    this.router.navigate(['/gestion-interna/eventos/entregas', entrega.idEntrega]);
  }
}
