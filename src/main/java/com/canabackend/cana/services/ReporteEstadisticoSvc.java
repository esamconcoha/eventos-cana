package com.canabackend.cana.services;

import java.time.LocalDate;

public interface ReporteEstadisticoSvc {

    /** El mismo tablero que devuelve {@link ReportesSvc}, en PDF con graficas. */
    byte[] generarPdf(LocalDate desde, LocalDate hasta);
}
