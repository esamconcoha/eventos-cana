export interface EventoProximo {
  correlativoPedido: string;
  fechaEvento: string;
  nombreCliente?: string | null;
  nombreTipoEvento?: string | null;
  ubicacion?: string | null;
  codigoEstadoPedido?: string | null;
  nombreEstadoPedido?: string | null;
}

// Todos los contadores salen de un dato real. No hay "stock bajo" porque
// items_cana no tiene un mínimo configurable contra el cual comparar; lo que
// sí existe es cantidad_faltantes, y eso es lo que se cuenta.
export interface DashboardResumen {
  cotizacionesPendientes: number;
  articulosConFaltantes: number;
  eventosProximos7Dias: number;
  usuariosActivos: number;
  entregasHoy: number;
  recoleccionesSinAgendar: number;
  proximosEventos: EventoProximo[];
}
