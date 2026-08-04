package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadoCuentaPedidoDto {
    String correlativoPedido;
    Double totalPedido;
    Double totalPagado;
    Double saldoPendiente;
    String estadoPago;
}
