import { DetalleServicioPedido, EstadoActualPedido } from './pedido';

export type { EstadoActualPedido };

// entregas_pedido no tiene columna de estado: se deriva comparando
// pedidos_cana.fecha_entrega (la fecha PACTADA, no la ejecutada) contra hoy,
// más pedidoFinalizado. Ver categoriaFechaEntrega() en entrega.service.ts —
// esa es la única implementación, no duplicar la comparación de fechas.
export type EstadoEntregaDerivado = 'HOY' | 'PROGRAMADA' | 'ATRASADA' | 'SIN_FECHA' | 'FINALIZADA';

// El backend no manda idViaje acá: los items vienen anidados dentro de su
// viaje, el padre ya lo implica. Mismo criterio con idEntrega en DetalleViaje.
export interface DetalleViajeItem {
  idDetalle: number;
  idItem: number;
  nombreItem: string;   // solo lectura, join con items_cana
  cantidadItem: number;
}

export interface DetalleViaje {
  idViaje: number;
  fechaInicioViaje: string;
  fechaFinViaje?: string | null;
  items?: DetalleViajeItem[];
}

// Item del pedido con su avance de envío. Alimenta el selector de "Registrar
// viaje" — NO es el catálogo items_cana, es el detalle_pedido de ESE correlativo.
export interface EntregaItemPendiente {
  idItem: number;
  nombreItem: string;
  cantidadPedida: number;
  cantidadEnviada: number;
  cantidadPendiente: number;
}

export interface EntregaPedido {
  idEntrega: number;
  correlativoPedido: string;
  nombreClientePedido?: string;   // solo lectura, join con pedidos_cana
  direccionPedido?: string;       // solo lectura, join con pedidos_cana
  fechaEntrega?: string | null;   // pedidos_cana.fecha_entrega (pactada); fecha "yyyy-mm-dd" pura, sin hora/zona
  fechaEvento?: string | null;    // solo lectura, join con pedidos_cana
  cantidadViajesAproximados: number;
  cantidadViajesReales: number;
  pedidoFinalizado: boolean;
  fechaInicioEntrega?: string | null;   // ejecución: cuándo arrancó el primer viaje registrado
  fechaFinEntrega?: string | null;      // ejecución: cuándo se marcó finalizada

  // Estado logístico del pedido, con la misma forma en listarEntregas y en
  // obtenerEntrega. Solo lo escriben automáticamente: creación → CNF, primer
  // viaje → RE, marcarFinalizada → ETR. EP/M/PR/REC se ponen a mano desde
  // Pedidos (cambiarEstado). Viene null si el pedido no tiene estado abierto.
  estadoActualPedido?: EstadoActualPedido | null;
  salonEntrega?: number | null;              // solo en obtenerEntrega

  /** Servicios contratados; vacío si el pedido solo alquiló mobiliario. */
  servicios?: DetalleServicioPedido[];
  viajes?: DetalleViaje[];
  items?: EntregaItemPendiente[];   // solo en obtenerEntrega, no en listarEntregas

  /** Si ya se subió la constancia firmada por el cliente. Solo en obtenerEntrega. */
  constanciaFirmadaDisponible?: boolean;
}

// Los contadores los calcula el backend; no derivar ninguno en el frontend
// para no tener dos fuentes de verdad sobre el mismo tablero.
// enCurso cambió de significado: ya no es "toda entrega no finalizada", es
// "entregas de HOY" (fecha_entrega === hoy). Ver categoriaFechaEntrega().
export interface EstadisticasEntregas {
  enCurso: number;      // no finalizadas con fecha_entrega === hoy
  programadas: number;  // no finalizadas con fecha_entrega > hoy
  atrasadas: number;    // no finalizadas con fecha_entrega < hoy
  sinFecha: number;     // no finalizadas con fecha_entrega null
  finalizadas: number;
  viajesHoy: number;
  desvioViajes: number;
}

export interface RegistrarViaje {
  idEntrega: number;
  fechaInicioViaje: string;
  fechaFinViaje: string | null;
  items: { idItem: number; cantidadItem: number }[];
}
