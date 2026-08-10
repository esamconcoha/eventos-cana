import { DetalleViaje } from './entrega';
import { EstadoActualPedido } from './pedido';

// Un item visto desde la vuelta. El tope no es lo pedido sino lo que
// efectivamente SALIÓ: no se puede traer algo que nunca se entregó.
// Cuando la recolección se cierra, lo que quede pendiente es el faltante.
export interface ItemRecoleccion {
  idItem: number;
  nombreItem: string;
  cantidadEntregada: number;
  cantidadRecolectada: number;
  cantidadPendiente: number;
}

export interface RecoleccionDetalle {
  idRecoleccion: number;
  correlativoPedido: string;
  nombreClientePedido?: string | null;
  direccionPedido?: string | null;
  cantidadViajesAproximados: number;
  cantidadViajesReales: number;
  recoleccionFinalizada: boolean;
  fechaInicioRecoleccion?: string | null;
  fechaFinRecoleccion?: string | null;
  fechaEvento?: string | null;
  /** pedidos_cana.fecha_recogido: fecha PROGRAMADA, "yyyy-mm-dd" pura. */
  fechaRecoleccion?: string | null;
  salonEntrega?: number | null;
  estadoActualPedido?: EstadoActualPedido | null;

  items: ItemRecoleccion[];
  viajes: DetalleViaje[];

  /** Mientras está abierta es "por recolectar"; al cerrarla, el faltante. */
  totalPendiente: number;
  itemsConPendiente: number;
}

export interface ProgramarRecoleccion {
  fechaRecoleccion: string;
  cantidadViajesAproximados?: number;
}
