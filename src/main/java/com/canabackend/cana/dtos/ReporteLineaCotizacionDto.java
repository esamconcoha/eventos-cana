package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteLineaCotizacionDto {
    private String tipo;
    private String descripcion;
    private String especificaciones;
    private Double cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
