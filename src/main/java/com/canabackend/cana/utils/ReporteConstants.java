package com.canabackend.cana.utils;

public final class ReporteConstants {

    private ReporteConstants() {
    }

    /** Cuantas filas trae cada top. Es un reporte de lectura, no un listado. */
    public static final int LIMITE_TOP = 10;

    /** La cartera admite mas filas: es la lista de cobro, se trabaja completa. */
    public static final int LIMITE_CARTERA = 15;

    /**
     * Tope de meses de la serie. Doce meses es un ano movil; mas alla la grafica
     * deja de leerse y ese analisis es el que se hara en Power BI.
     */
    public static final int MAX_MESES_SERIE = 24;

    /** Recursos del reporte Jasper. */
    public static final String REPORTE_ESTADISTICO_JRXML = "reports/estadisticas.jrxml";
    public static final String REPORTE_LOGO = "reports/logo.png";

    public static final String CONTENT_TYPE_PDF = "application/pdf";
}
