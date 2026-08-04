package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una porcion de una grafica de composicion (tipo de evento, estado del pedido,
 * estado de pago). Lleva las dos lecturas del mismo grupo: cuantos son y cuanto
 * dinero representan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDistribucionDto {
    private String etiqueta;
    private Long cantidad;
    private Double monto;
    /** Participacion sobre el total del grupo, en porcentaje. */
    private Double porcentaje;
}
