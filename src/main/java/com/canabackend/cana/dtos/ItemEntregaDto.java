package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Un item del pedido visto desde la logistica de entrega: cuanto se pidio,
 * cuanto ya viajo y cuanto falta. Es la lista que alimenta el selector de
 * items al registrar un viaje (no el catalogo completo de items_cana).
 */
@Data
@AllArgsConstructor
public class ItemEntregaDto {
    Long idItem;
    String nombreItem;
    Double cantidadPedida;
    Double cantidadEnviada;
    Double cantidadPendiente;
}
