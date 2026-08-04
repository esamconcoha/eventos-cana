package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViajeItemDto {
    Long idDetalle;
    Long idItem;
    String nombreItem;
    Double cantidadItem;
}
