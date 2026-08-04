package com.canabackend.cana.exceptions;

public enum ErrorEnum {
    I_DESCONOCIDO                           (1000, EstadoHttp.INTERNAL_SERVER_ERROR, "Error desconocido, comuniquese con el administrador del sitio"),
    NO_AUTORIZADO                           (1001, EstadoHttp.FORBIDDEN, "No autorizado"),
    I_BAD_CREDENTIALS                       (1002, EstadoHttp.UNAUTHORIZED, "Las credenciales proporcionadas no son validas"),
    I_USUARIO_EXISTENTE                     (1003, EstadoHttp.BAD_REQUEST, "El usuario ya existe en el sistema"),
    USUARIO_NOT_FOUND                       (1004, EstadoHttp.BAD_REQUEST, "Error Usuario no encontrado"),
    USUARIO_INACTIVO                        (1005, EstadoHttp.FORBIDDEN, "El usuario esta registrado pero no se encuentra activo"),
    DESCRIPCION_ITEM_EXISTENTE              (2000, EstadoHttp.BAD_REQUEST, "La descripcion del item ya existe en el sistema"),
    NOMBRE_SERVICIO_EXISTENTE               (2001, EstadoHttp.BAD_REQUEST, "El servicio ya existe en el sistema"),
    COTIZACION_NOT_FOUND                    (3000, EstadoHttp.BAD_REQUEST, "La cotizacion solicitada no existe"),
    I_ERROR_GENERAR_DOCUMENTO               (3001, EstadoHttp.INTERNAL_SERVER_ERROR, "Ocurrio un error al generar el documento de la cotizacion"),
    DOCUMENTO_NOT_FOUND                     (3002, EstadoHttp.BAD_REQUEST, "No se encontro el documento solicitado"),
    I_ERROR_GENERAR_CONSTANCIA              (3003, EstadoHttp.INTERNAL_SERVER_ERROR, "Ocurrio un error al generar la constancia de entrega"),
    I_ERROR_GENERAR_REPORTE                 (3004, EstadoHttp.INTERNAL_SERVER_ERROR, "Ocurrio un error al generar el reporte estadistico"),
    PEDIDO_NOT_FOUND                        (4000, EstadoHttp.BAD_REQUEST, "El pedido solicitado no existe"),
    PAGO_NOT_FOUND                          (4001, EstadoHttp.BAD_REQUEST, "El pago solicitado no existe"),
    MONTO_PAGO_INVALIDO                     (4002, EstadoHttp.BAD_REQUEST, "El monto del pago debe ser mayor a cero"),
    TIPO_PAGO_INVALIDO                      (4003, EstadoHttp.BAD_REQUEST, "El tipo de pago no es valido"),
    METODO_PAGO_INVALIDO                    (4004, EstadoHttp.BAD_REQUEST, "El metodo de pago no es valido"),
    PAGO_EXCEDE_SALDO_PENDIENTE             (4005, EstadoHttp.BAD_REQUEST, "El monto del pago excede el saldo pendiente del pedido"),
    ESTADO_PEDIDO_NOT_FOUND                 (4006, EstadoHttp.BAD_REQUEST, "El estado de pedido solicitado no existe"),
    ESTADO_PEDIDO_INICIAL_NO_CONFIGURADO    (4007, EstadoHttp.INTERNAL_SERVER_ERROR, "El catalogo de estados de pedido no tiene configurado el estado inicial"),
    COTIZACION_YA_CONVERTIDA                (4008, EstadoHttp.BAD_REQUEST, "La cotizacion ya fue convertida a un pedido"),
    SALON_NOT_FOUND                         (4009, EstadoHttp.BAD_REQUEST, "El salon solicitado no existe"),
    ITEM_PEDIDO_NOT_FOUND                   (4010, EstadoHttp.BAD_REQUEST, "Uno de los items del pedido no existe o no esta activo"),
    SERVICIO_PEDIDO_NOT_FOUND               (4011, EstadoHttp.BAD_REQUEST, "Uno de los servicios del pedido no existe o no esta activo"),
    ENTREGA_NOT_FOUND                       (4012, EstadoHttp.BAD_REQUEST, "La entrega solicitada no existe"),
    ENTREGA_YA_EXISTE                       (4013, EstadoHttp.BAD_REQUEST, "El pedido ya tiene una entrega registrada"),
    ENTREGA_YA_FINALIZADA                   (4014, EstadoHttp.BAD_REQUEST, "La entrega ya fue finalizada y no admite cambios"),
    CANTIDAD_VIAJES_INVALIDA                (4015, EstadoHttp.BAD_REQUEST, "La cantidad de viajes aproximados debe ser mayor a cero"),
    VIAJE_SIN_ITEMS                         (4016, EstadoHttp.BAD_REQUEST, "El viaje debe llevar al menos un item"),
    CANTIDAD_ITEM_VIAJE_INVALIDA            (4017, EstadoHttp.BAD_REQUEST, "La cantidad de cada item del viaje debe ser mayor a cero"),
    ITEM_NO_PERTENECE_AL_PEDIDO             (4018, EstadoHttp.BAD_REQUEST, "Uno de los items del viaje no pertenece al pedido de esta entrega"),
    CANTIDAD_ITEM_EXCEDE_PENDIENTE          (4019, EstadoHttp.BAD_REQUEST, "La cantidad a enviar supera lo que queda pendiente de ese item"),
    FECHAS_VIAJE_INVALIDAS                  (4020, EstadoHttp.BAD_REQUEST, "La fecha de inicio del viaje es obligatoria y no puede ser posterior a la de fin"),
    ENTREGA_SIN_VIAJES                      (4021, EstadoHttp.BAD_REQUEST, "No se puede finalizar una entrega que no tiene viajes registrados"),
    ESTADO_ENTREGA_NO_CONFIGURADO           (4022, EstadoHttp.INTERNAL_SERVER_ERROR, "El catalogo de estados no tiene configurado el estado de entrega requerido"),
    FECHA_ENTREGA_REQUERIDA                 (4023, EstadoHttp.BAD_REQUEST, "La fecha de entrega es obligatoria"),
    RANGO_CALENDARIO_INVALIDO               (4024, EstadoHttp.BAD_REQUEST, "El rango de fechas del calendario no es valido"),
    RECOLECCION_NOT_FOUND                   (4025, EstadoHttp.BAD_REQUEST, "La recoleccion solicitada no existe"),
    RECOLECCION_YA_FINALIZADA               (4026, EstadoHttp.BAD_REQUEST, "La recoleccion ya fue finalizada y no admite cambios"),
    RECOLECCION_SIN_VIAJES                  (4027, EstadoHttp.BAD_REQUEST, "No se puede finalizar una recoleccion que no tiene viajes registrados"),
    CANTIDAD_ITEM_EXCEDE_ENTREGADO          (4028, EstadoHttp.BAD_REQUEST, "No se puede recolectar mas de lo que se entrego de ese articulo"),
    ITEM_NO_FUE_ENTREGADO                   (4029, EstadoHttp.BAD_REQUEST, "Ese articulo nunca salio en un viaje de entrega de este pedido"),
    FECHA_RECOLECCION_REQUERIDA             (4030, EstadoHttp.BAD_REQUEST, "La fecha de recoleccion es obligatoria"),
    FECHA_EN_PASADO                         (4031, EstadoHttp.BAD_REQUEST, "La fecha no puede ser anterior a hoy"),
    COTIZACION_NO_EDITABLE                  (4032, EstadoHttp.BAD_REQUEST, "Solo se puede modificar una cotizacion que aun no ha sido confirmada"),
    ESTADO_CANCELADO_NO_CONFIGURADO         (4033, EstadoHttp.INTERNAL_SERVER_ERROR, "El catalogo de estados no tiene configurado el estado de evento cancelado"),
    PEDIDO_YA_CANCELADO                     (4034, EstadoHttp.BAD_REQUEST, "El pedido ya se encuentra cancelado"),
    RANGO_REPORTE_INVALIDO                  (4035, EstadoHttp.BAD_REQUEST, "El rango de fechas del reporte no es valido"),
    ARCHIVO_VACIO                           (4036, EstadoHttp.BAD_REQUEST, "El archivo esta vacio"),
    TIPO_ARCHIVO_NO_PERMITIDO               (4037, EstadoHttp.BAD_REQUEST, "El archivo debe ser PDF, JPG, PNG o WEBP"),
    ARCHIVO_EXCEDE_TAMANO_MAXIMO            (4038, EstadoHttp.BAD_REQUEST, "El archivo no puede superar los 10MB"),
    CONSTANCIA_FIRMADA_NOT_FOUND            (4039, EstadoHttp.BAD_REQUEST, "Esta entrega todavia no tiene una constancia firmada subida"),
    I_ERROR_SUBIR_CONSTANCIA_FIRMADA        (4040, EstadoHttp.INTERNAL_SERVER_ERROR, "Ocurrio un error al guardar la constancia firmada");
//______________________________________________________________________________
    /**
     * <p>Codigo de error.</p>
     */
    private final int codigo;
//______________________________________________________________________________
    /**
     * <p>Codigo de estado Http.</p>
     */
    private final int estadoHttp;
//______________________________________________________________________________
    /**
     * <p>Descripcion del error.</p>
     */
    private final String descripcion;
//______________________________________________________________________________
    /**
     * <p>Constructor de la enumeracion, establece el valor de los atributos.</p>
     *
     * @param pCodigo Codigo de error
     * @param pEstadoHttp Codigo de estado de Http.
     * @param pMensaje Descripcion de error
     */
    private ErrorEnum(int pCodigo, int pEstadoHttp, String pMensaje) {
        this.codigo = pCodigo;
        this.estadoHttp = pEstadoHttp;
        this.descripcion = pMensaje;
    }
//______________________________________________________________________________
    /**
     * <p>Codigo que identifica el error de forma unica.</p>
     *
     * @return int Codigo de error
     */
    public int getCodigo() {
        return codigo;
    }
//______________________________________________________________________________
    /**
     * <p>Codigo de estado Http.</p>
     *
     * @return int Estado Http
     */
    public int getEstadoHttp() {
        return estadoHttp;
    }
//______________________________________________________________________________
    /**
     * <p>Descripcion del error, proporciona un punto de inicio para que el
     * usuario sepa que correcciones realizar.</p>
     *
     * @return String Descripcion del error
     */
    public String getDescripcion() {
        return descripcion;
    }
}
