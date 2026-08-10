export type EstadoPagoPedido = 'PENDIENTE' | 'ANTICIPO' | 'PARCIAL' | 'PAGADO' | 'DEVUELTO';

// estados_pedido.id_estado es FK numérico a tipo_estado (tipo_estado = 'EVE').
// No se usa un código de texto inventado: el id real del catálogo es la clave.
export type OrigenPedido = 'COTIZACION' | 'DIRECTO';

export interface DetallePedido {
  idItem: number;
  nombreItem?: string;      // solo para mostrar en UI
  costoItem?: number;       // solo para mostrar en UI
  cantidadItemPedido: number;
}

export interface DetalleServicioPedido {
  idServicio: number;
  nombreServicio?: string;  // solo para mostrar en UI
  cantidad: number;
  precioAcordado: number;
  especificaciones?: string;

  /** Identifica la línea, para poder confirmarla realizada. Solo lectura. */
  idDetalleServPedido?: number;
  /**
   * Momento en que se confirmó realizado el servicio; null = pendiente.
   * Reemplaza al estado "Montando / Decorando": el montaje es paralelo a los
   * viajes de entrega y opcional, y un estado no admite ninguna de las dos.
   */
  fechaRealizado?: string | null;
}

// El backend expone dos formas distintas, no una sola:
//  - estadoActual (dentro de Pedido): solo id/codigo/nombre, sin fechas.
//  - historialEstados/{correlativo}: agrega las fechas del tramo.
// Ninguna de las dos trae correlativo_estado, aunque la columna exista en BD.
export interface EstadoActualPedido {
  idEstado: number;           // FK a estados.id_estado
  codigoEstado?: string;      // estados.codigo_estado, solo lectura
  nombreEstado?: string;      // estados.nombre_estado, solo lectura
}

export interface EstadoPedidoHistorial extends EstadoActualPedido {
  fechaHoraInicio: string;
  fechaHoraFin?: string | null;
}

export interface Pedido {
  correlativoPedido: string;
  direccionPedido: string;
  /**
   * Código del estado ACTUAL del ciclo de vida ('CNF', 'EP', ... 'ECA').
   * Antes era un booleano "activo/inactivo" que nadie ponía nunca en false;
   * desde sql/008 guarda el código y el historial lo mantiene un trigger.
   * Para mostrarlo en pantalla se usa estadoActual, que ya trae nombre y color.
   */
  estadoPedido: string;
  dpiUsuarioPedido: string;
  nombreClientePedido?: string;   // join con usuarios, solo lectura
  telefonoClientePedido?: number; // pedidos_cana.telefono_cliente_pedido (int4), solo lectura
  fechaEvento: string;
  fechaEntrega?: string | null;
  salonEntrega?: number | null;   // FK a mantenimiento_salones.id_salon
  nombreSalon?: string;           // solo lectura, join con mantenimiento_salones
  fechaConfirmacionPedido?: string | null;
  /** pedidos_cana.fecha_recogido: fecha PROGRAMADA de recolección. */
  fechaRecoleccion?: string | null;
  pagado: boolean;
  usuarioInternoPedido: string;
  codTipoEvento: string;
  nombreTipoEvento?: string;      // solo lectura
  estadoPago: EstadoPagoPedido;
  idCotizacion?: number | null;   // FK a cotizaciones; la asigna el backend al confirmar una cotización
  origenPedido?: OrigenPedido;    // derivable de idCotizacion != null
  montoTotalPedido?: number;
  saldoPendiente?: number;
  estadoActual?: EstadoActualPedido;
  detalles?: DetallePedido[];
  detallesServicios?: DetalleServicioPedido[];
}

// Pedido "Directo": el cliente se captura como texto libre (igual que en
// cotizaciones), no se selecciona de usuarios existentes. usuarioInternoPedido
// sí es el staff logueado (se toma del token, no se pide en el formulario).
// fechaEntrega y cantidadViajesAproximados son obligatorios: el backend los
// usa para crear de una vez el registro en entregas_pedido (ya no existe un
// paso manual de "Nueva entrega").
export interface CrearPedido {
  nombreClientePedido: string;
  telefonoClientePedido: number;
  dpiUsuarioPedido: string;
  usuarioInternoPedido: string;
  direccionPedido: string;
  fechaEvento: string;
  salonEntrega?: number;   // FK a mantenimiento_salones.id_salon
  fechaEntrega: string;
  cantidadViajesAproximados: number;
  /** Opcional: se puede agendar acá o después desde Recolecciones. */
  fechaRecoleccion?: string;
  codTipoEvento: string;
  detalles: { idItem: number; cantidadItemPedido: number }[];
  detallesServicios: { idServicio: number; cantidad: number; precioAcordado: number; especificaciones?: string }[];
}

// Reemplaza por completo detalles/detallesServicios (delete + recreate en backend),
// no es un merge incremental por idDetalle.
export interface ActualizarPedido {
  direccionPedido?: string;
  salonEntrega?: number;   // FK a mantenimiento_salones.id_salon
  fechaEntrega?: string;
  fechaRecoleccion?: string;
  fechaEvento?: string;
  detalles?: { idItem: number; cantidadItemPedido: number }[];
  detallesServicios?: { idServicio: number; cantidad: number; precioAcordado: number; especificaciones?: string }[];
}
