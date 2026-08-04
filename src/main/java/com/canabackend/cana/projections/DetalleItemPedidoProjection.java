package com.canabackend.cana.projections;

/** Linea de articulo de un pedido, con el nombre del item resuelto. */
public interface DetalleItemPedidoProjection {
    Long getIdItem();
    String getNombreItem();
    Double getCostoItem();
    Double getCantidadItemPedido();
}
