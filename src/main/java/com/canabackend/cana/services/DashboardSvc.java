package com.canabackend.cana.services;

import com.canabackend.cana.dtos.DashboardResumenDto;

public interface DashboardSvc {

    /** Contadores y proximos eventos de la pantalla de inicio, en una llamada. */
    DashboardResumenDto obtenerResumen();
}
