package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgramarRecoleccionDto {
    /** Va a pedidos_cana.fecha_recogido. Obligatoria. */
    LocalDate fechaRecoleccion;
    /** Opcional: si no viene, se conserva la estimacion actual. */
    Integer cantidadViajesAproximados;
}
