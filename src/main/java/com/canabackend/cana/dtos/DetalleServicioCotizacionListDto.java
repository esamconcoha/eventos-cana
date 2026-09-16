package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetalleServicioCotizacionListDto {
    Long idServicio;
    String nombreServicio;
    double cantidad;
    double precioCotizado;
    String especificaciones;

    /** NIN | POR | MON | EXO. Ver DescuentoConstants. */
    String tipoDescuento;
    Double valorDescuento;
    String motivoDescuento;
    /** Ya resuelto en la BD: lo que descuenta esta linea, en Q. */
    Double montoDescuento;
    /** precioCotizado x cantidad - montoDescuento. Es la cifra que factura. */
    Double subtotalNeto;
}
