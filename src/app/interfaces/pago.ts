export type EstadoCuentaPedido = 'PENDIENTE' | 'ANTICIPO' | 'PARCIAL' | 'PAGADO' | 'DEVUELTO';

export interface PagoPedido {
  idPago: number;
  correlativoPedido: string;
  montoPago: number;
  fechaPago: string;
  tipoPago: string;          // código catálogo TIPO_PAGO (abono, devolución, etc.)
  nombreTipoPago?: string;   // solo lectura
  metodoPago: string;        // efectivo, transferencia, tarjeta...
  referenciaPago?: string;
  usuarioRegistro: string;
  estadoRegistro: boolean;
  fechaCreo: string;
}

export interface RegistrarPago {
  correlativoPedido: string;
  montoPago: number;
  tipoPago: string;
  metodoPago: string;
  referenciaPago?: string;
  usuarioRegistro: string;
}

// Refleja EstadoCuentaPedidoDto tal cual: los nombres tienen que calzar con el
// JSON del backend o los montos llegan como undefined y la tarjeta sale vacia.
export interface EstadoCuenta {
  correlativoPedido: string;
  /** Suma de las lineas ANTES de descuentos. */
  totalBruto: number;
  /** Lo descontado entre articulos y servicios. */
  totalDescuentos: number;
  /** totalBruto - totalDescuentos: lo que realmente se cobra. */
  totalPedido: number;
  totalPagado: number;
  saldoPendiente: number;
  estadoPago: EstadoCuentaPedido;
}
