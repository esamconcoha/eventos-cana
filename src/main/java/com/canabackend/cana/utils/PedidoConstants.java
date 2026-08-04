package com.canabackend.cana.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PedidoConstants {

    private PedidoConstants() {
    }

    /** tipo_estado en la tabla estados para el ciclo de vida del pedido/evento. */
    public static final String TIPO_ESTADO_EVENTO = "EVE";

    public static final String ESTADO_EVENTO_CONFIRMADO = "CNF";
    public static final String ESTADO_EVENTO_EN_PROCESO = "EP";
    /** @deprecated desactivado en sql/007: el montaje es paralelo y opcional,
     *  vive en detalle_servicio_pedido.fecha_realizado. */
    @Deprecated
    public static final String ESTADO_EVENTO_MONTANDO = "M";
    public static final String ESTADO_EVENTO_EN_RUTA_ENTREGA = "RE";
    public static final String ESTADO_EVENTO_ENTREGADO = "ETR";
    /** @deprecated desactivado en sql/007: se deriva de entregas_pedido
     *  (fila REC sin fecha_recogido), no hace falta un estado que lo repita. */
    @Deprecated
    public static final String ESTADO_EVENTO_PENDIENTE_RECOLECCION = "PR";
    public static final String ESTADO_EVENTO_RECOLECTADO = "REC";
    public static final String ESTADO_EVENTO_FINALIZADO = "FIN";
    /** Estado terminal: el pedido/evento fue cancelado. Se entra por "Cancelar pedido". */
    public static final String ESTADO_EVENTO_CANCELADO = "ECA";

    /** Estado con el que arranca todo pedido nuevo (no existe un "creado sin confirmar" en este catalogo). */
    public static final String ESTADO_EVENTO_INICIAL = ESTADO_EVENTO_CONFIRMADO;

    public static final String PREFIJO_CORRELATIVO_PEDIDO = "PED-";

    /**
     * Estados en los que un pedido todavia admite que se le abra una entrega.
     * Quedan fuera ETR/PR/REC/FIN: ahi la mercaderia ya salio o el evento cerro.
     */
    public static final List<String> ESTADOS_EVENTO_ADMITEN_ENTREGA = Collections.unmodifiableList(
            Arrays.asList(ESTADO_EVENTO_CONFIRMADO, ESTADO_EVENTO_EN_PROCESO,
                    ESTADO_EVENTO_EN_RUTA_ENTREGA));
}
