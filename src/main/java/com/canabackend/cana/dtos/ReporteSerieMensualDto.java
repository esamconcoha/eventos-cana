package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Un mes de la serie. {@code periodo} viene como "yyyy-MM". */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteSerieMensualDto {
    private String periodo;
    /** "Ene 25", ya armado en el backend para que la grafica no lo recalcule. */
    private String etiqueta;
    private Long eventos;
    private Double facturado;
    private Double cobrado;
}
