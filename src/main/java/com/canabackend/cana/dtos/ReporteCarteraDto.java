package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Pedido del periodo que todavia debe dinero. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteCarteraDto {
    private String correlativoPedido;
    private String cliente;
    private LocalDateTime fechaEvento;
    private Double total;
    private Double pagado;
    private Double saldo;
    private String estadoPago;
}
