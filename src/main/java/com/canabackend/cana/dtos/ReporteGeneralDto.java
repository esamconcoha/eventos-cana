package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Todo el tablero de reporteria en una sola respuesta. Es una foto del negocio,
 * no una herramienta de analisis: la inteligencia de negocio (cruces libres,
 * proyecciones, cohortes) se llevara en Power BI sobre la misma base.
 *
 * <p>El periodo se aplica sobre la fecha del evento. El dinero cobrado es el de
 * los pedidos de ese periodo, sin importar cuando entro el pago, para que
 * facturado - cobrado sea exactamente el saldo que se reporta.
 */
@Data
public class ReporteGeneralDto {

    private LocalDate desde;
    private LocalDate hasta;

    private ReporteResumenDto resumen;

    /** Serie continua: los meses sin eventos vienen en cero, no ausentes. */
    private List<ReporteSerieMensualDto> serieMensual;

    private List<ReporteDistribucionDto> eventosPorTipo;
    private List<ReporteDistribucionDto> eventosPorEstado;
    private List<ReporteDistribucionDto> carteraPorEstadoPago;

    private List<ReporteRankingDto> topArticulos;
    private List<ReporteRankingDto> topServicios;
    private List<ReporteClienteDto> topClientes;

    private List<ReporteCarteraDto> pedidosConSaldo;

    /** Foto actual de bodega, independiente del periodo consultado. */
    private List<ReporteRankingDto> articulosConFaltantes;
}
