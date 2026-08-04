package com.canabackend.cana.projections;

/** Avance de despacho item por item de una entrega: lo pedido contra lo enviado. */
public interface ItemEntregaProjection {
    Long getIdItem();
    String getNombreItem();
    Double getCantidadPedida();
    Double getCantidadEnviada();
}
