package com.canabackend.cana.utils;

public final class CotizacionConstants {

    private CotizacionConstants() {
    }

    public static final String TIPO_ESTADO_COTIZACION = "COT";

    public static final String ESTADO_CREADA = "C";
    public static final String ESTADO_PENDIENTE_CONFIRMAR = "P";
    public static final String ESTADO_CONFIRMADA = "CONF";
    public static final String ESTADO_CANCELADA = "CAN";
    public static final String ESTADO_ELIMINADA = "E";

    /**
     * Estados en los que la cotizacion todavia se puede modificar. Una vez
     * confirmada ya genero un pedido con sus detalles copiados: editarla
     * dejaria el documento y el pedido diciendo cosas distintas.
     */
    public static boolean esEditable(String estadoCotizacion) {
        return ESTADO_CREADA.equals(estadoCotizacion)
                || ESTADO_PENDIENTE_CONFIRMAR.equals(estadoCotizacion);
    }

    /** Recursos del reporte Jasper. */
    public static final String REPORTE_COTIZACION_JRXML = "reports/cotizacion.jrxml";
    public static final String REPORTE_LOGO = "reports/logo.png";

    /** Tipo de documento generado y persistido. */
    public static final String TIPO_DOCUMENTO_COTIZACION = "COTIZACION";
    public static final String CONTENT_TYPE_PDF = "application/pdf";

    /**
     * Devuelve el nombre legible del estado de una cotizacion a partir de su
     * codigo, para mostrarlo en el documento.
     */
    public static String nombreEstado(String codigoEstado) {
        if (codigoEstado == null) {
            return "";
        }
        switch (codigoEstado) {
            case ESTADO_CREADA:
                return "Creada";
            case ESTADO_PENDIENTE_CONFIRMAR:
                return "Pendiente de Confirmar";
            case ESTADO_CONFIRMADA:
                return "Confirmada";
            case ESTADO_CANCELADA:
                return "Cancelada";
            case ESTADO_ELIMINADA:
                return "Eliminada";
            default:
                return codigoEstado;
        }
    }
}
