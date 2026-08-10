export type EstadoCotizacion = 'C' | 'P' | 'CONF' | 'CAN';

export interface DetalleCotizacion {
  idItem: number;
  nombreItem?: string;       // solo para mostrar en UI
  costoItem?: number;        // solo para mostrar en UI
  cantidadItemCotizacion: number;
  subtotal?: number;         // calculado en UI
}

export interface DetalleServicioCotizacion {
  idServicio: number;
  nombreServicio?: string;   // solo para mostrar en UI
  cantidad: number;
  precioCotizado: number;
  especificaciones?: string;
}

export interface CrearCotizacion {
  nombreClienteCotizacion: string;
  direccionClienteCotizacion: string;
  telefonoClienteCotizacion: number;
  fechaHoraEvento: string;            // ISO datetime del evento (lo captura el usuario)
  cotizacionConfirmada: boolean;      // siempre false al crear
  dpiNitUsuarioCotizacion: string;
  codTipoEvento: string;              // codigo del catalogo TIPO_EVENTOS
  detalleCotizacion: DetalleCotizacion[];
  detalleServicioCotizacion: DetalleServicioCotizacion[];
}

// Se piden al confirmar porque el backend usa estos datos para crear de una
// vez el registro en entregas_pedido (ya no existe un paso manual "Nueva entrega").
// Solo se puede modificar mientras no esté confirmada: una vez confirmada ya
// generó un pedido con los detalles copiados, y editarla dejaría el documento
// y el pedido diciendo cosas distintas.
export function cotizacionEsEditable(estado: EstadoCotizacion | undefined): boolean {
  return estado === 'C' || estado === 'P';
}

// Reemplaza por completo detalleCotizacion y detalleServicioCotizacion
// (delete + recreate en backend), no es un merge línea por línea.
export interface ActualizarCotizacion {
  nombreClienteCotizacion: string;
  direccionClienteCotizacion: string;
  telefonoClienteCotizacion: number;
  fechaHoraEvento: string;
  codTipoEvento: string;
  detalleCotizacion: DetalleCotizacion[];
  detalleServicioCotizacion: DetalleServicioCotizacion[];
}

export interface ConfirmarCotizacion {
  fechaEntregaEstimada: string;
  cantidadViajesAproximados: number;
}

export interface Cotizacion {
  idCotizacion: number;
  codigoCotizacion: string;           // correlativo generado en BD, solo lectura
  nombreClienteCotizacion: string;
  direccionClienteCotizacion: string;
  telefonoClienteCotizacion: string;
  fechaCotizacion: string;            // fecha de registro, la asigna el backend
  fechaHoraEvento: string;
  estadoCotizacion: EstadoCotizacion;
  dpiNitUsuarioCotizacion: string;
  /** Necesario para precargar el formulario al modificarla. */
  codTipoEvento?: string;
  /** true si el pedido generado por esta cotización (ya confirmada) fue cancelado. */
  pedidoCancelado?: boolean;
  detalles?: DetalleCotizacion[];
  detallesServicios?: DetalleServicioCotizacion[];
}
