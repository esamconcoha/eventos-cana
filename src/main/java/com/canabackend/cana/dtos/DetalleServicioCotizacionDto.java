package com.canabackend.cana.dtos;

import lombok.Data;

@Data
public class DetalleServicioCotizacionDto {
    Long idServicio;
    double cantidad;
    double precioCotizado;
    String especificaciones;

    /** NIN | POR | MON | EXO. Ausente equivale a NIN (sin descuento). */
    String tipoDescuento;
    /** Porcentaje (0..100) si el tipo es POR, monto en Q si es MON. */
    Double valorDescuento;
    String motivoDescuento;
}
