package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetallePedidoListDto {
    Long idItem;
    String nombreItem;
    Double costoItem;
    Double cantidadItemPedido;

    /** NIN | POR | MON | EXO. Ver DescuentoConstants. */
    String tipoDescuento;
    Double valorDescuento;
    String motivoDescuento;
    /** Ya resuelto en la BD: lo que descuenta esta linea, en Q. */
    Double montoDescuento;
    /** costoItem x cantidad - montoDescuento. Es la cifra que factura. */
    Double subtotalNeto;
}
