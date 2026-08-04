package com.canabackend.cana.utils;

public final class CalendarioConstants {

    private CalendarioConstants() {
    }

    /** Fecha y hora del evento en si (pedidos_cana.fecha_evento). */
    public static final String TIPO_EVENTO = "EVENTO";

    /** Fecha pactada de entrega (pedidos_cana.fecha_entrega). */
    public static final String TIPO_ENTREGA = "ENTREGA";

    /** Fecha programada de recoleccion (pedidos_cana.fecha_recogido). */
    public static final String TIPO_RECOLECCION = "RECOLECCION";

    // No hay TIPO_VISITA_TECNICA a proposito: no existe tabla ni columna que
    // lo respalde, y una categoria que nunca pinta nada confunde mas de lo
    // que ayuda.
}
