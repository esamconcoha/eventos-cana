/**
 * Descuento a nivel de línea (artículo o servicio), tanto en cotizaciones como
 * en pedidos. El backend lo guarda en las cuatro tablas de detalle y lo copia
 * tal cual al confirmar una cotización, así que la trazabilidad no se pierde.
 *
 *  NIN  sin descuento (default de toda línea, incluidas las históricas)
 *  POR  porcentaje sobre el subtotal de la línea (0..100)
 *  MON  monto fijo en Q sobre el subtotal de la línea
 *  EXO  exoneración total: la línea queda en Q 0.00
 */
export type TipoDescuento = 'NIN' | 'POR' | 'MON' | 'EXO';

/** Campos que el frontend ENVÍA por cada línea. */
export interface DescuentoLineaRequest {
  tipoDescuento?: TipoDescuento;
  /** Porcentaje si el tipo es POR, monto en Q si es MON; 0 en NIN y EXO. */
  valorDescuento?: number;
  motivoDescuento?: string;
}

/** Campos extra que el backend DEVUELVE por cada línea, ya calculados en BD. */
export interface DescuentoLineaResponse extends DescuentoLineaRequest {
  /** Lo que descuenta la línea, en Q. */
  montoDescuento?: number;
  /** Subtotal ya con el descuento aplicado; es lo que suma el total. */
  subtotalNeto?: number;
}
