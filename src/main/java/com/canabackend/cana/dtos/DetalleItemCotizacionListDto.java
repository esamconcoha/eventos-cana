package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetalleItemCotizacionListDto {
    Long idItem;
    String nombreItem;
    Double costoItem;
    Double cantidadItemCotizacion;
}
