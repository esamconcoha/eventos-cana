package com.canabackend.cana.services;

import com.canabackend.cana.dtos.CalendarioItemDto;

import java.time.LocalDate;
import java.util.List;

public interface CalendarioSvc {

    /**
     * Agenda entre dos fechas inclusive: eventos y entregas mezclados en una
     * sola lista ordenada, para que el calendario se arme con una sola llamada.
     */
    List<CalendarioItemDto> obtenerAgenda(LocalDate desde, LocalDate hasta);
}
