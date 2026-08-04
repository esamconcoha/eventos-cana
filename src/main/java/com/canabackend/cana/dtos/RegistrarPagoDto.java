package com.canabackend.cana.dtos;

import lombok.Data;

@Data
public class RegistrarPagoDto {
    String correlativoPedido;
    Double montoPago;
    String tipoPago;
    String metodoPago;
    String referenciaPago;
    String usuarioRegistro;
}
