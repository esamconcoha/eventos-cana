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
    /** Subtotal ANTES del descuento. */
    private Double subtotal;
    private Double montoDescuento;
    /** Texto corto del descuento ("-20% (Cliente frecuente)"); null si no lleva. */
    private String etiquetaDescuento;
    /** Subtotal ya con el descuento aplicado; es el que suma el TOTAL. */
    private Double subtotalNeto;
}
