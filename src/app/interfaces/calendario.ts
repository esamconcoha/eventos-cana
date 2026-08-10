// Solo existen los tipos que tienen un dato real detrás:
//   EVENTO      ← pedidos_cana.fecha_evento (timestamp, con hora)
//   ENTREGA     ← pedidos_cana.fecha_entrega (date, sin hora)
//   RECOLECCION ← pedidos_cana.fecha_recogido (date, sin hora)
// Visita técnica sigue fuera: no hay tabla ni columna que la respalde, y una
// categoría que nunca pinta nada confunde más de lo que ayuda.
export type TipoCalendario = 'EVENTO' | 'ENTREGA' | 'RECOLECCION';

export interface CalendarioItem {
  tipo: TipoCalendario;
  fecha: string;                 // "yyyy-mm-dd", clave de agrupación de la grilla
  fechaHora?: string | null;     // null en ENTREGA: la fecha pactada no tiene hora
  titulo: string;
  correlativoPedido: string;
  nombreCliente?: string | null;
  ubicacion?: string | null;
  codigoEstadoPedido?: string | null;
  nombreEstadoPedido?: string | null;

  // Solo ENTREGA
  idEntrega?: number | null;
  entregaFinalizada?: boolean | null;
  cantidadViajesReales?: number | null;
  cantidadViajesAproximados?: number | null;
}

export interface TipoCalendarioDef {
  tipo: TipoCalendario;
  label: string;
  dot: string;
  chip: string;       // chip dentro de la celda del día
  badge: string;      // badge en el panel lateral
}

export const TIPOS_CALENDARIO: TipoCalendarioDef[] = [
  {
    tipo: 'EVENTO',
    label: 'Evento',
    dot: 'bg-purple-500',
    chip: 'bg-purple-500 text-white',
    badge: 'bg-purple-50 text-purple-700 ring-purple-200'
  },
  {
    tipo: 'ENTREGA',
    label: 'Entrega',
    dot: 'bg-amber-400',
    chip: 'bg-amber-400 text-amber-950',
    badge: 'bg-amber-50 text-amber-700 ring-amber-200'
  },
  {
    tipo: 'RECOLECCION',
    label: 'Recolección',
    dot: 'bg-sky-500',
    chip: 'bg-sky-500 text-white',
    badge: 'bg-sky-50 text-sky-700 ring-sky-200'
  }
];

export function tipoCalendarioDef(tipo: TipoCalendario): TipoCalendarioDef {
  return TIPOS_CALENDARIO.find(t => t.tipo === tipo)!;
}
