// Catálogo de estados del ciclo de vida del pedido (cana.estados, tipo_estado = 'EVE').
//
// Es la ÚNICA definición de etiquetas y colores. Pedidos y Entregas la comparten
// a propósito: el mismo pedido no puede mostrarse con dos nombres distintos según
// la pantalla en la que esté parado el operador.
//
// El nombre a mostrar preferentemente viene del backend (estadoActual.nombreEstado,
// leído de cana.estados). Acá vive solo lo que la BD no tiene: color y orden.
//
// Los colores se buscan por codigoEstado, no por idEstado: el código es estable,
// mientras que el id depende de cómo se haya sembrado el catálogo.

export interface EstadoLogisticoDef {
  idEstado: number;       // cana.estados.id_estado
  codigoEstado: string;   // cana.estados.codigo_estado
  label: string;          // respaldo si el backend no manda nombreEstado
  dot: string;            // color del punto indicador
  badgeClass: string;     // colores del badge completo
}

// Orden = avance del flujo logístico.
//
// No están 'M' (Montando/Decorando) ni 'PR' (Pendiente de recolección): se
// desactivaron en sql/007. El montaje es paralelo a los viajes y opcional, así
// que vive en detalle_servicio_pedido.fecha_realizado; y "pendiente de
// recolección" ya se deriva de entregas_pedido (fila REC sin fecha_recogido),
// no hacía falta un estado que repitiera el mismo hecho a mano.
export const ESTADOS_LOGISTICOS: EstadoLogisticoDef[] = [
  { idEstado: 6,  codigoEstado: 'CNF', label: 'Confirmado',                     dot: 'bg-slate-400',   badgeClass: 'bg-slate-50 text-slate-600 ring-slate-200' },
  { idEstado: 7,  codigoEstado: 'EP',  label: 'En proceso',                     dot: 'bg-amber-400',   badgeClass: 'bg-amber-50 text-amber-700 ring-amber-200' },
  { idEstado: 8,  codigoEstado: 'RE',  label: 'En Ruta de entrega',             dot: 'bg-sky-400',     badgeClass: 'bg-sky-50 text-sky-700 ring-sky-200' },
  { idEstado: 10, codigoEstado: 'ETR', label: 'Entregado',                      dot: 'bg-emerald-500', badgeClass: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
  { idEstado: 12, codigoEstado: 'REC', label: 'Recolectado / en ruta a bodega', dot: 'bg-sky-500',     badgeClass: 'bg-sky-50 text-sky-700 ring-sky-200' },
  { idEstado: 13, codigoEstado: 'FIN', label: 'Finalizado',                     dot: 'bg-slate-500',   badgeClass: 'bg-slate-50 text-slate-600 ring-slate-200' },
  // Terminal: NO es un paso del flujo lineal (por eso siguienteEstadoLogistico
  // lo excluye del "avanzar"), pero sí se lista en el combo de estado y en el
  // filtro. id 20 = cana.estados (sembrado a mano).
  { idEstado: 20, codigoEstado: 'ECA', label: 'Evento cancelado',               dot: 'bg-red-500',     badgeClass: 'bg-red-50 text-red-600 ring-red-200' }
];

// "Evento cancelado" (ECA) es un estado TERMINAL: se lista en el combo y en el
// filtro, pero queda fuera del avance lineal (ver siguienteEstadoLogistico). Se
// detecta por su código, que es estable, y no por id.
export const CODIGO_ESTADO_CANCELADO = 'ECA';
export const ESTADO_CANCELADO_DEF: EstadoLogisticoDef =
  ESTADOS_LOGISTICOS.find(e => e.codigoEstado === CODIGO_ESTADO_CANCELADO)!;

export function esCodigoCancelado(codigo: string | undefined | null): boolean {
  return codigo === CODIGO_ESTADO_CANCELADO;
}

export function estadoLogisticoPorCodigo(codigo: string | undefined | null): EstadoLogisticoDef | undefined {
  return codigo ? ESTADOS_LOGISTICOS.find(e => e.codigoEstado === codigo) : undefined;
}

export function estadoLogisticoPorId(idEstado: number | undefined | null): EstadoLogisticoDef | undefined {
  return idEstado != null ? ESTADOS_LOGISTICOS.find(e => e.idEstado === idEstado) : undefined;
}
