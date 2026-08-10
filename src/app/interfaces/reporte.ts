// Espejo de los DTOs de /reportes/privado/general.
//
// Criterio del backend (vale para todo el módulo): el periodo se aplica sobre la
// fecha del evento, y lo cobrado es el de esos pedidos sin importar cuándo entró
// el pago. Así facturado − cobrado siempre es el saldo que se muestra.

export interface ReporteResumen {
  eventos: number;
  eventosCancelados: number;
  eventosFinalizados: number;
  facturado: number;
  cobrado: number;
  saldoPorCobrar: number;
  pedidosConSaldo: number;
  ticketPromedio: number;
  cotizaciones: number;
  cotizacionesConfirmadas: number;
  tasaConversion: number;
  porcentajeCobrado: number;
  tasaCancelacion: number;
  articulosConFaltantes: number;
  unidadesFaltantes: number;
}

export interface ReporteSerieMensual {
  periodo: string;
  etiqueta: string;
  eventos: number;
  facturado: number;
  cobrado: number;
}

export interface ReporteDistribucion {
  etiqueta: string;
  cantidad: number;
  monto: number;
  porcentaje: number;
}

export interface ReporteRanking {
  etiqueta: string;
  categoria: string | null;
  cantidad: number;
  monto: number;
  eventos: number | null;
  disponible: number | null;
}

export interface ReporteCliente {
  nombre: string;
  eventos: number;
  facturado: number;
  cobrado: number;
  saldo: number;
}

export interface ReporteCartera {
  correlativoPedido: string;
  cliente: string;
  fechaEvento: string;
  total: number;
  pagado: number;
  saldo: number;
  estadoPago: string;
}

export interface ReporteGeneral {
  desde: string;
  hasta: string;
  resumen: ReporteResumen;
  serieMensual: ReporteSerieMensual[];
  eventosPorTipo: ReporteDistribucion[];
  eventosPorEstado: ReporteDistribucion[];
  carteraPorEstadoPago: ReporteDistribucion[];
  topArticulos: ReporteRanking[];
  topServicios: ReporteRanking[];
  topClientes: ReporteCliente[];
  pedidosConSaldo: ReporteCartera[];
  /** Foto actual de bodega, no del periodo consultado. */
  articulosConFaltantes: ReporteRanking[];
}

/** Rango preseleccionado del filtro. 'personalizado' habilita los date-picker. */
export type PresetPeriodo = 'mesActual' | 'trimestre' | 'semestre' | 'anioMovil' | 'anioActual' | 'personalizado';
