package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cliente del ranking de facturacion del periodo. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteClienteDto {
    private String nombre;
    private Long eventos;
    private Double facturado;
    private Double cobrado;
    private Double saldo;
}
