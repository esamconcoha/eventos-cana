package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Un item visto desde la vuelta: cuanto salio, cuanto volvio y cuanto falta.
 * El pendiente de una recoleccion ya cerrada es el faltante.
 */
@Data
@AllArgsConstructor
public class ItemRecoleccionDto {
    Long idItem;
    String nombreItem;
    Double cantidadEntregada;
    Double cantidadRecolectada;
    Double cantidadPendiente;
}
