package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PagoPedidoListDto {
    Long idPago;
    Double montoPago;
    LocalDateTime fechaPago;
    String tipoPago;
    String nombreTipoPago;
    String metodoPago;
    String nombreMetodoPago;
    String referenciaPago;
    String usuarioRegistro;
    Boolean estadoRegistro;
}
