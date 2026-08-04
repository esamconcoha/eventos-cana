package com.canabackend.cana.dtos;

import lombok.Data;

/** Indicadores de cabecera del reporte. Ninguno es estimado. */
@Data
public class ReporteResumenDto {

    /** Eventos del periodo que no estan cancelados. */
    private Long eventos;
    private Long eventosCancelados;
    private Long eventosFinalizados;

    /** Valor de los pedidos no cancelados: items + servicios. */
    private Double facturado;
    /** Pagos activos de esos pedidos, neto de devoluciones. */
    private Double cobrado;
    /** facturado - cobrado. */
    private Double saldoPorCobrar;
    private Long pedidosConSaldo;

    /** facturado / eventos. Cero si no hubo eventos. */
    private Double ticketPromedio;

    private Long cotizaciones;
    private Long cotizacionesConfirmadas;
    /** Porcentaje de cotizaciones del periodo que terminaron en pedido. */
    private Double tasaConversion;

    /** Porcentaje de lo facturado que ya se cobro. */
    private Double porcentajeCobrado;

    /** Porcentaje de eventos del periodo que se cancelaron. */
    private Double tasaCancelacion;

    /** Foto de bodega: articulos activos con unidades faltantes registradas. */
    private Long articulosConFaltantes;
    private Long unidadesFaltantes;
}
