package com.canabackend.cana.services;

import com.canabackend.cana.dtos.ReporteGeneralDto;

import java.time.LocalDate;

public interface ReportesSvc {

    /**
     * Tablero completo del periodo (ambos extremos inclusive). Si no se envian
     * fechas se asumen los ultimos 12 meses.
     */
    ReporteGeneralDto obtenerReporteGeneral(LocalDate desde, LocalDate hasta);
}
