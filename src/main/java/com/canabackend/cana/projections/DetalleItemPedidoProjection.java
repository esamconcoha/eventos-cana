package com.canabackend.cana.projections;

/** Linea de articulo de un pedido, con el nombre del item y el descuento resueltos. */
public interface DetalleItemPedidoProjection {
    Long getIdItem();
    String getNombreItem();
    Double getCostoItem();
    Double getCantidadItemPedido();
    String getTipoDescuento();
    Double getValorDescuento();
    String getMotivoDescuento();
    /** Lo calcula cana.fn_descuento_linea, no el servicio. */
    Double getMontoDescuento();
    Double getSubtotalNeto();
}
