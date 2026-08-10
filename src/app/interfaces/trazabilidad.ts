// Módulo de trazabilidad: convierte el historial de estados que devuelve el
// backend (tablas estados_pedido / estados_cotizacion, alimentadas por los
// triggers de sql/008) en algo mostrable: avance %, pasos del flujo y duraciones.
//
// Pedidos y cotizaciones comparten TODO menos el catálogo de estados, así que
// acá se define una forma común (TrazaFlujo) y el componente de UI es uno solo.

import { ESTADOS_LOGISTICOS, CODIGO_ESTADO_CANCELADO } from './estado-logistico';

/** Un tramo del historial: el pedido/cotización estuvo en este estado desde-hasta. */
export interface TrazaEvento {
  idEstado: number;
  codigoEstado?: string;
  nombreEstado?: string;
  fechaHoraInicio: string;
  fechaHoraFin?: string | null;   // null = tramo abierto (estado actual)
}

export interface TrazaEstadoDef {
  codigo: string;
  label: string;
  /**
   * Etiqueta breve para el stepper, donde cada paso tiene ~55px de ancho.
   * Con el label completo ("Recolectado / en ruta a bodega") los textos de
   * pasos contiguos se pisan entre sí en pantallas angostas.
   */
  corto: string;
  dot: string;
  badgeClass: string;
  /**
   * Posición en el flujo lineal, o null si el estado está FUERA del flujo
   * (cancelado/eliminado). Los estados fuera de flujo no aportan avance: el
   * progreso se congela en el último estado real que se alcanzó.
   */
  paso: number | null;
}

export interface TrazaFlujo {
  /** Etiqueta del tipo de entidad, para títulos. */
  titulo: string;
  /** Pasos del flujo lineal, en orden. Alimenta el stepper. */
  pasos: TrazaEstadoDef[];
  /** Todos los estados posibles, incluidos los terminales y los de legado. */
  defs: TrazaEstadoDef[];
}

// ─── Flujo de PEDIDOS (cana.estados, tipo_estado = 'EVE') ──────────────────
// Los colores NO se redefinen acá: se derivan de ESTADOS_LOGISTICOS, que es la
// única definición de color/etiqueta del ciclo de vida del pedido. Duplicarlos
// llevaría a que el mismo estado se vea de un color en la tabla y de otro en
// la trazabilidad.
/** Solo para el stepper; el nombre completo sigue saliendo de ESTADOS_LOGISTICOS. */
const CORTO_PEDIDO: Record<string, string> = {
  CNF: 'Confirmado',
  EP:  'En proceso',
  RE:  'En ruta',
  ETR: 'Entregado',
  REC: 'Recolectado',
  FIN: 'Finalizado'
};

const PASOS_PEDIDO: TrazaEstadoDef[] = ESTADOS_LOGISTICOS
  .filter(e => e.codigoEstado !== CODIGO_ESTADO_CANCELADO)
  .map((e, i) => ({
    codigo: e.codigoEstado,
    label: e.label,
    corto: CORTO_PEDIDO[e.codigoEstado] ?? e.label,
    dot: e.dot,
    badgeClass: e.badgeClass,
    paso: i
  }));

const DEF_CANCELADO_PEDIDO: TrazaEstadoDef = {
  codigo: CODIGO_ESTADO_CANCELADO,
  label: 'Evento cancelado',
  corto: 'Cancelado',
  dot: 'bg-red-500',
  badgeClass: 'bg-red-50 text-red-600 ring-red-200',
  paso: null
};

export const FLUJO_PEDIDO: TrazaFlujo = {
  titulo: 'Pedido',
  pasos: PASOS_PEDIDO,
  defs: [...PASOS_PEDIDO, DEF_CANCELADO_PEDIDO]
};

// ─── Flujo de COTIZACIONES (cana.estados, tipo_estado = 'COT') ─────────────
// El flujo vivo es P → CONF. "Creada" (C) ya no se usa (las cotizaciones nacen
// en P), pero sigue apareciendo en el historial viejo, así que necesita
// etiqueta y color: se le da el mismo paso que P para no inventar un escalón
// que hoy no existe.
const PASOS_COTIZACION: TrazaEstadoDef[] = [
  { codigo: 'P',    label: 'Pendiente de confirmar', corto: 'Pendiente',  dot: 'bg-amber-400',   badgeClass: 'bg-amber-50 text-amber-700 ring-amber-200',       paso: 0 },
  { codigo: 'CONF', label: 'Confirmada',             corto: 'Confirmada', dot: 'bg-emerald-500', badgeClass: 'bg-emerald-50 text-emerald-700 ring-emerald-200', paso: 1 }
];

export const FLUJO_COTIZACION: TrazaFlujo = {
  titulo: 'Cotización',
  pasos: PASOS_COTIZACION,
  defs: [
    ...PASOS_COTIZACION,
    { codigo: 'C',   label: 'Creada',    corto: 'Creada',    dot: 'bg-slate-400',  badgeClass: 'bg-slate-50 text-slate-600 ring-slate-200',    paso: 0 },
    { codigo: 'CAN', label: 'Cancelada', corto: 'Cancelada', dot: 'bg-red-500',    badgeClass: 'bg-red-50 text-red-600 ring-red-200',          paso: null },
    { codigo: 'E',   label: 'Eliminada', corto: 'Eliminada', dot: 'bg-purple-500', badgeClass: 'bg-purple-50 text-purple-700 ring-purple-200', paso: null }
  ]
};

export function defEstado(flujo: TrazaFlujo, codigo: string | undefined | null): TrazaEstadoDef | undefined {
  return codigo ? flujo.defs.find(d => d.codigo === codigo) : undefined;
}

/** true si el estado saca a la entidad del flujo (cancelada / eliminada). */
export function esEstadoFueraDeFlujo(flujo: TrazaFlujo, codigo: string | undefined | null): boolean {
  const def = defEstado(flujo, codigo);
  return !!def && def.paso === null;
}

/**
 * Porcentaje de avance del estado indicado dentro del flujo.
 * Un estado fuera de flujo devuelve null: el avance no lo define él, lo define
 * el último estado real por el que pasó (ver avanceHistorial).
 */
export function porcentajeDeEstado(flujo: TrazaFlujo, codigo: string | undefined | null): number | null {
  const def = defEstado(flujo, codigo);
  if (!def || def.paso === null) { return null; }
  return Math.round(((def.paso + 1) / flujo.pasos.length) * 100);
}

/**
 * Avance a partir del historial COMPLETO.
 *
 * Si la entidad terminó cancelada, el progreso NO se pone en 0 ni en 100: se
 * congela en lo que alcanzó antes de cancelarse, que es lo que realmente pasó.
 */
export function avanceHistorial(flujo: TrazaFlujo, historial: TrazaEvento[]): number {
  let maximo = 0;
  for (const ev of historial) {
    const pct = porcentajeDeEstado(flujo, ev.codigoEstado);
    if (pct !== null && pct > maximo) { maximo = pct; }
  }
  return maximo;
}

// ─── Duraciones ────────────────────────────────────────────────────────────

/** Milisegundos que duró un tramo. Si sigue abierto, cuenta hasta ahora. */
export function duracionMs(evento: TrazaEvento): number {
  const inicio = new Date(evento.fechaHoraInicio).getTime();
  const fin = evento.fechaHoraFin ? new Date(evento.fechaHoraFin).getTime() : Date.now();
  return Math.max(0, fin - inicio);
}

/**
 * Duración legible y compacta: "3 d 4 h", "5 h 20 min", "12 min".
 * Se corta en dos unidades a propósito: en una tabla de tiempos, el detalle de
 * segundos no aporta y alarga la fila.
 */
export function formatearDuracion(ms: number): string {
  const minutos = Math.floor(ms / 60000);
  if (minutos < 1) { return 'menos de 1 min'; }
  if (minutos < 60) { return `${minutos} min`; }

  const horas = Math.floor(minutos / 60);
  if (horas < 24) {
    const restoMin = minutos % 60;
    return restoMin ? `${horas} h ${restoMin} min` : `${horas} h`;
  }

  const dias = Math.floor(horas / 24);
  const restoHoras = horas % 24;
  return restoHoras ? `${dias} d ${restoHoras} h` : `${dias} d`;
}

/** Tiempo total desde que arrancó el primer estado hasta hoy (o hasta el cierre). */
export function duracionTotalMs(historial: TrazaEvento[]): number {
  if (!historial.length) { return 0; }
  const inicios = historial.map(e => new Date(e.fechaHoraInicio).getTime());
  const primerInicio = Math.min(...inicios);

  // Si ya no hay ningún tramo abierto, el ciclo terminó: se mide hasta el
  // último cierre y no hasta ahora, que seguiría creciendo para siempre.
  const abierto = historial.some(e => !e.fechaHoraFin);
  if (abierto) { return Date.now() - primerInicio; }

  const fines = historial.map(e => new Date(e.fechaHoraFin as string).getTime());
  return Math.max(...fines) - primerInicio;
}
