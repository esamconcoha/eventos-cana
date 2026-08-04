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
}
