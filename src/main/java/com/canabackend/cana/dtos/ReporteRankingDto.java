package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Linea de un top: articulos rentados, servicios vendidos o faltantes de bodega. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRankingDto {
    private String etiqueta;
    private String categoria;
    /** Unidades: rentadas, vendidas o faltantes segun el listado. */
    private Double cantidad;
    /** Dinero que representan esas unidades. */
    private Double monto;
    /** En cuantos eventos distintos aparecio. Null en el listado de faltantes. */
    private Long eventos;
    /** Existencias declaradas en bodega. Null cuando no aplica (servicios). */
    private Long disponible;
}
