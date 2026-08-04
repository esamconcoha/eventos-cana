package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Una linea de la constancia de entrega: puede ser un articulo despachado o un
 * servicio contratado. No lleva precios: es un comprobante de lo entregado, no
 * un documento de cobro.
 */
@Data
@AllArgsConstructor
public class ReporteLineaEntregaDto {
    private String tipo;
    private String descripcion;
    private String especificaciones;
    private Double cantidad;
}
