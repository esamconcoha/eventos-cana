package com.canabackend.cana.utils;

/**
 * Tipos de movimiento logistico. Entrega y recoleccion comparten la tabla
 * entregas_pedido (y todo el arbol detalle_viaje / detalle_viaje_items): un
 * pedido tiene a lo sumo una fila de cada tipo.
 */
public final class MovimientoConstants {

    private MovimientoConstants() {
    }

    /** Ida: lo que sale de bodega hacia el evento. */
    public static final String TIPO_ENTREGA = "ENT";

    /** Vuelta: lo que regresa a bodega despues del evento. */
    public static final String TIPO_RECOLECCION = "REC";
}
