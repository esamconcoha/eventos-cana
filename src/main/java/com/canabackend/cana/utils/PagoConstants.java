package com.canabackend.cana.utils;

public final class PagoConstants {

    private PagoConstants() {
    }

    /** nombre_catalogo en tipo_catalogos_cana bajo el que estan los codigos de tipo_pago. */
    public static final String NOMBRE_CATALOGO_TIPO_PAGO = "TIPO_PAGO";

    public static final String TIPO_PAGO_ANTICIPO = "AN";
    public static final String TIPO_PAGO_ABONO = "ABO";
    public static final String TIPO_PAGO_PAGO_FINAL = "PF";
    public static final String TIPO_PAGO_DEVOLUCION = "DEV";

    /** tipo_estado en la tabla estados bajo el que estan los codigos de estado_pago. */
    public static final String TIPO_ESTADO_PAGO = "PAGO";

    public static final String ESTADO_PAGO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_PAGO_ANTICIPO = "ANTICIPO";
    public static final String ESTADO_PAGO_PARCIAL = "PARCIAL";
    public static final String ESTADO_PAGO_PAGADO = "PAGADO";
    public static final String ESTADO_PAGO_DEVUELTO = "DEVUELTO";

    /** id_tipo_catalogo en catalogos_cana bajo el que estan los codigos de metodo_pago. */
    public static final Long ID_TIPO_CATALOGO_METODO_PAGO = 5L;

    public static final String METODO_PAGO_EFECTIVO = "EFE";
    public static final String METODO_PAGO_TRANSFERENCIA = "TRAN";
    public static final String METODO_PAGO_TARJETA = "TAR";
    public static final String METODO_PAGO_DEPOSITO = "DEP";
}
