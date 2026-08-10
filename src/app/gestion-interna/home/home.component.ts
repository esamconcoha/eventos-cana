import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { DashboardService } from '../../services/dashboard.service';
import { TokenService } from '../../services/token.service';
import { DashboardResumen, EventoProximo } from '../../interfaces/dashboard';
import { estadoLogisticoPorCodigo } from '../../interfaces/estado-logistico';

interface TarjetaKpi {
  label: string;
  valor: number;
  icono: string;
  /** Clases del recuadro del icono. */
  chip: string;
  /** Color del número cuando el valor pide atención. */
  destacar: boolean;
  /** Indicador de contexto, no una tarea: se muestra más apagado. */
  secundaria: boolean;
  ruta: string;
}

interface AccesoRapido {
  label: string;
  icono: string;
  ruta: string;
}

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  resumen: DashboardResumen | null = null;
  cargando = false;
  nombreUsuario = '';
  rol = '';
  hoyLargo = '';

  // Accesos directos a las tareas más frecuentes del día. No pretende reflejar
  // todo el menú: es una lista corta de atajos, no un índice de pantallas.
  accesos: AccesoRapido[] = [
    { label: 'Nueva cotización', icono: 'request_quote',     ruta: '/gestion-interna/eventos/cotizaciones' },
    { label: 'Nuevo pedido',     icono: 'local_mall',        ruta: '/gestion-interna/eventos/pedidos' },
    { label: 'Ajustar inventario', icono: 'inventory_2',     ruta: '/gestion-interna/administracion/inventario' },
    { label: 'Ver calendario',   icono: 'calendar_month',    ruta: '/gestion-interna/eventos/calendario' }
  ];

  constructor(
    private dashboardService: DashboardService,
    private tokenService: TokenService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.nombreUsuario = this.tokenService.getUserName() ?? '';
    // El nombre del rol, no el id: antes se mostraba "1" en el badge.
    this.rol = this.tokenService.getNombreRol();
    this.hoyLargo = this.fechaLarga(new Date());
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.dashboardService.obtenerResumen().subscribe({
      next: (data) => {
        this.resumen = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  /**
   * Una sola tira de indicadores. Primero las cuatro tareas del día, después
   * los dos de contexto en gris: el orden ya comunica la prioridad, sin
   * necesidad de una tarjeta aparte que dejaba media columna vacía.
   */
  get tarjetas(): TarjetaKpi[] {
    if (!this.resumen) { return []; }
    return [
      {
        label: 'Entregas de hoy', valor: this.resumen.entregasHoy,
        icono: 'local_shipping', chip: 'bg-amber-100 text-amber-700',
        destacar: this.resumen.entregasHoy > 0, secundaria: false,
        ruta: '/gestion-interna/eventos/entregas'
      },
      {
        label: 'Recolecciones sin agendar', valor: this.resumen.recoleccionesSinAgendar,
        icono: 'assignment_return', chip: 'bg-sky-100 text-sky-700',
        destacar: this.resumen.recoleccionesSinAgendar > 0, secundaria: false,
        ruta: '/gestion-interna/eventos/recolecciones'
      },
      {
        label: 'Cotizaciones pendientes', valor: this.resumen.cotizacionesPendientes,
        icono: 'request_quote', chip: 'bg-purple-100 text-purple-700',
        destacar: false, secundaria: false,
        ruta: '/gestion-interna/eventos/cotizaciones'
      },
      {
        label: 'Artículos con faltantes', valor: this.resumen.articulosConFaltantes,
        icono: 'report_problem', chip: 'bg-red-100 text-red-600',
        destacar: this.resumen.articulosConFaltantes > 0, secundaria: false,
        ruta: '/gestion-interna/administracion/inventario'
      },
      {
        label: 'Eventos próximos 7 días', valor: this.resumen.eventosProximos7Dias,
        icono: 'event', chip: 'bg-slate-100 text-slate-500',
        destacar: false, secundaria: true,
        ruta: '/gestion-interna/eventos/calendario'
      },
      {
        label: 'Usuarios activos', valor: this.resumen.usuariosActivos,
        icono: 'people', chip: 'bg-slate-100 text-slate-500',
        destacar: false, secundaria: true,
        ruta: '/gestion-interna/administracion/usuarios'
      }
    ];
  }

  ir(ruta: string): void {
    this.router.navigate([ruta]);
  }

  // ─── Presentación ──────────────────────────────────────────
  estadoDot(evento: EventoProximo): string {
    return estadoLogisticoPorCodigo(evento.codigoEstadoPedido)?.dot ?? 'bg-slate-300';
  }

  tituloEvento(evento: EventoProximo): string {
    const tipo = evento.nombreTipoEvento ?? 'Evento';
    return evento.nombreCliente ? `${tipo} — ${evento.nombreCliente}` : tipo;
  }

  /** "21" y "JUL" separados, para el bloque de fecha del listado. */
  diaDe(fechaIso: string): string {
    return fechaIso.slice(8, 10);
  }

  mesDe(fechaIso: string): string {
    const [y, m, d] = fechaIso.slice(0, 10).split('-').map(Number);
    return new Date(y, m - 1, d)
      .toLocaleDateString('es', { month: 'short' })
      .replace('.', '')
      .toUpperCase();
  }

  horaDe(fechaIso: string): string {
    return fechaIso.length >= 16 ? fechaIso.slice(11, 16) : '';
  }

  /**
   * Se arma con partes locales: new Date("yyyy-mm-dd") se interpreta como UTC
   * medianoche y en UTC-6 mostraría el día anterior.
   */
  private fechaLarga(fecha: Date): string {
    const texto = fecha.toLocaleDateString('es', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    });
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  }
}
