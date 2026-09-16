package com.canabackend.cana.models;

import com.canabackend.cana.utils.DescuentoConstants;

/**
 * Linea de detalle que admite descuento: los articulos y servicios, tanto de
 * cotizacion como de pedido.
 *
 * <p>Existe para que las cuatro entidades compartan una unica normalizacion.
 * Sin esto, el mismo bloque de "validar tipo, forzar valor, limpiar motivo"
 * quedaria copiado en los dos servicios y con el tiempo se separarian (que es
 * justo como se cuela un descuento invalido a la BD).
 */
public interface LineaConDescuento {

    void setTipoDescuento(String tipoDescuento);

    void setValorDescuento(double valorDescuento);

    void setMotivoDescuento(String motivoDescuento);

    /**
     * Aplica el descuento que llega del cliente, ya normalizado y validado.
     * Un tipo ausente equivale a "sin descuento", asi que llamar a esto sin
     * datos deja la linea exactamente como estaba antes de la funcionalidad.
     */
    default void aplicarDescuento(String tipo, Double valor, String motivo) {
        String tipoNormalizado = DescuentoConstants.normalizarTipo(tipo);
        setTipoDescuento(tipoNormalizado);
        setValorDescuento(DescuentoConstants.normalizarValor(tipoNormalizado, valor));
        setMotivoDescuento(DescuentoConstants.normalizarMotivo(tipoNormalizado, motivo));
    }
}
