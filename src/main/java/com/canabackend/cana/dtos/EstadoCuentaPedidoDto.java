package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadoCuentaPedidoDto {
    String correlativoPedido;
    /** Suma de las lineas ANTES de descuentos. */
    Double totalBruto;
    /** Lo que se descuento en total entre articulos y servicios. */
    Double totalDescuentos;
    /** totalBruto - totalDescuentos: lo que realmente se cobra. */
    Double totalPedido;
    Double totalPagado;
    Double saldoPendiente;
    String estadoPago;
}
