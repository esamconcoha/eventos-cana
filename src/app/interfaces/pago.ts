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

export interface EstadoCuenta {
  correlativoPedido: string;
  montoTotalPedido: number;
  totalPagado: number;
  saldoPendiente: number;
  estadoPago: EstadoCuentaPedido;
  pagado: boolean;
}
