package com.canabackend.cana.projections;

/** Contadores de cabecera del reporte estadistico (una sola consulta). */
public interface ReporteResumenProjection {
    Long getEventos();
    Long getEventosCancelados();
    Long getEventosFinalizados();
    Double getFacturado();
    Double getCobrado();
    Long getPedidosConSaldo();
    Long getCotizaciones();
    Long getCotizacionesConfirmadas();
    Long getArticulosConFaltantes();
    Long getUnidadesFaltantes();
}
