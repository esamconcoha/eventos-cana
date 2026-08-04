package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.ReporteCarteraDto;
import com.canabackend.cana.dtos.ReporteClienteDto;
import com.canabackend.cana.dtos.ReporteDistribucionDto;
import com.canabackend.cana.dtos.ReporteGeneralDto;
import com.canabackend.cana.dtos.ReporteRankingDto;
import com.canabackend.cana.dtos.ReporteResumenDto;
import com.canabackend.cana.dtos.ReporteSerieMensualDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.projections.ReporteCarteraProjection;
import com.canabackend.cana.projections.ReporteClienteProjection;
import com.canabackend.cana.projections.ReporteDistribucionProjection;
import com.canabackend.cana.projections.ReporteRankingProjection;
import com.canabackend.cana.projections.ReporteResumenProjection;
import com.canabackend.cana.projections.ReporteSerieMensualProjection;
import com.canabackend.cana.repositories.ReportesRepository;
import com.canabackend.cana.services.ReportesSvc;
import com.canabackend.cana.utils.ReporteConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportesSvcImpl implements ReportesSvc {

    /** Rango por defecto cuando la pantalla no manda fechas: ano movil. */
    private static final int MESES_POR_DEFECTO = 11;

    private static final DateTimeFormatter FORMATO_PERIODO = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter FORMATO_ETIQUETA =
            DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("es"));

    @Autowired
    private ReportesRepository reportesRepository;

    @Override
    @Transactional(readOnly = true)
    public ReporteGeneralDto obtenerReporteGeneral(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1).minusMonths(MESES_POR_DEFECTO);

        if (inicio.isAfter(fin)) {
            throw new MSCanaException(ErrorEnum.RANGO_REPORTE_INVALIDO);
        }
        if (YearMonth.from(inicio).until(YearMonth.from(fin), ChronoUnit.MONTHS)
                >= ReporteConstants.MAX_MESES_SERIE) {
            throw new MSCanaException(ErrorEnum.RANGO_REPORTE_INVALIDO);
        }

        // Extremo derecho abierto: incluye todo el ultimo dia sin depender de
        // la hora del evento (fecha_evento es timestamp).
        LocalDateTime desdeHora = inicio.atStartOfDay();
        LocalDateTime hastaHora = fin.plusDays(1).atStartOfDay();

        ReporteGeneralDto dto = new ReporteGeneralDto();
        dto.setDesde(inicio);
        dto.setHasta(fin);
        dto.setResumen(construirResumen(desdeHora, hastaHora, inicio, fin));
        dto.setSerieMensual(construirSerie(desdeHora, hastaHora, inicio, fin));
        dto.setEventosPorTipo(distribucion(this.reportesRepository.eventosPorTipo(desdeHora, hastaHora), false));
        dto.setEventosPorEstado(distribucion(this.reportesRepository.eventosPorEstado(desdeHora, hastaHora), false));
        dto.setCarteraPorEstadoPago(distribucion(
                this.reportesRepository.carteraPorEstadoPago(desdeHora, hastaHora), false));
        dto.setTopArticulos(ranking(
                this.reportesRepository.topArticulos(desdeHora, hastaHora, ReporteConstants.LIMITE_TOP)));
        dto.setTopServicios(ranking(
                this.reportesRepository.topServicios(desdeHora, hastaHora, ReporteConstants.LIMITE_TOP)));
        dto.setTopClientes(clientes(
                this.reportesRepository.topClientes(desdeHora, hastaHora, ReporteConstants.LIMITE_TOP)));
        dto.setPedidosConSaldo(cartera(
                this.reportesRepository.pedidosConSaldo(desdeHora, hastaHora, ReporteConstants.LIMITE_CARTERA)));
        dto.setArticulosConFaltantes(ranking(
                this.reportesRepository.articulosConFaltantes(ReporteConstants.LIMITE_TOP)));
        return dto;
    }

    private ReporteResumenDto construirResumen(LocalDateTime desde, LocalDateTime hasta,
                                               LocalDate desdeFecha, LocalDate hastaFecha) {
        ReporteResumenProjection p =
                this.reportesRepository.obtenerResumen(desde, hasta, desdeFecha, hastaFecha);

        ReporteResumenDto resumen = new ReporteResumenDto();
        long eventos = valor(p != null ? p.getEventos() : null);
        long cancelados = valor(p != null ? p.getEventosCancelados() : null);
        long cotizaciones = valor(p != null ? p.getCotizaciones() : null);
        long confirmadas = valor(p != null ? p.getCotizacionesConfirmadas() : null);
        double facturado = valor(p != null ? p.getFacturado() : null);
        double cobrado = valor(p != null ? p.getCobrado() : null);

        resumen.setEventos(eventos);
        resumen.setEventosCancelados(cancelados);
        resumen.setEventosFinalizados(valor(p != null ? p.getEventosFinalizados() : null));
        resumen.setFacturado(redondear(facturado));
        resumen.setCobrado(redondear(cobrado));
        resumen.setSaldoPorCobrar(redondear(facturado - cobrado));
        resumen.setPedidosConSaldo(valor(p != null ? p.getPedidosConSaldo() : null));
        resumen.setTicketPromedio(eventos > 0 ? redondear(facturado / eventos) : 0d);
        resumen.setCotizaciones(cotizaciones);
        resumen.setCotizacionesConfirmadas(confirmadas);
        resumen.setTasaConversion(porcentaje(confirmadas, cotizaciones));
        resumen.setPorcentajeCobrado(porcentaje(cobrado, facturado));
        resumen.setTasaCancelacion(porcentaje(cancelados, eventos + cancelados));
        resumen.setArticulosConFaltantes(valor(p != null ? p.getArticulosConFaltantes() : null));
        resumen.setUnidadesFaltantes(valor(p != null ? p.getUnidadesFaltantes() : null));
        return resumen;
    }

    /**
     * La consulta solo devuelve meses con eventos. Se completan los vacios en
     * cero: un mes sin ventas es informacion, y sin el la grafica de linea
     * uniria dos meses no consecutivos como si nada hubiera pasado en medio.
     */
    private List<ReporteSerieMensualDto> construirSerie(LocalDateTime desde, LocalDateTime hasta,
                                                        LocalDate desdeFecha, LocalDate hastaFecha) {
        Map<String, ReporteSerieMensualProjection> porPeriodo = new LinkedHashMap<>();
        for (ReporteSerieMensualProjection p : this.reportesRepository.serieMensual(desde, hasta)) {
            porPeriodo.put(p.getPeriodo(), p);
        }

        List<ReporteSerieMensualDto> serie = new ArrayList<>();
        YearMonth ultimo = YearMonth.from(hastaFecha);
        for (YearMonth mes = YearMonth.from(desdeFecha); !mes.isAfter(ultimo); mes = mes.plusMonths(1)) {
            String periodo = mes.format(FORMATO_PERIODO);
            ReporteSerieMensualProjection p = porPeriodo.get(periodo);
            serie.add(new ReporteSerieMensualDto(
                    periodo,
                    capitalizar(mes.atDay(1).format(FORMATO_ETIQUETA).replace(".", "")),
                    p != null ? valor(p.getEventos()) : 0L,
                    p != null ? redondear(valor(p.getFacturado())) : 0d,
                    p != null ? redondear(valor(p.getCobrado())) : 0d));
        }
        return serie;
    }

    /**
     * @param porMonto si el porcentaje se reparte sobre el dinero en vez de
     *                 sobre la cantidad de registros.
     */
    private List<ReporteDistribucionDto> distribucion(
            List<ReporteDistribucionProjection> filas, boolean porMonto) {
        double total = 0;
        for (ReporteDistribucionProjection f : filas) {
            total += porMonto ? valor(f.getMonto()) : valor(f.getCantidad());
        }

        List<ReporteDistribucionDto> resultado = new ArrayList<>();
        for (ReporteDistribucionProjection f : filas) {
            double parte = porMonto ? valor(f.getMonto()) : valor(f.getCantidad());
            resultado.add(new ReporteDistribucionDto(
                    f.getEtiqueta(),
                    valor(f.getCantidad()),
                    redondear(valor(f.getMonto())),
                    porcentaje(parte, total)));
        }
        return resultado;
    }

    private List<ReporteRankingDto> ranking(List<ReporteRankingProjection> filas) {
        List<ReporteRankingDto> resultado = new ArrayList<>();
        for (ReporteRankingProjection f : filas) {
            resultado.add(new ReporteRankingDto(
                    f.getEtiqueta(), f.getCategoria(),
                    redondear(valor(f.getCantidad())), redondear(valor(f.getMonto())),
                    f.getEventos(), f.getDisponible()));
        }
        return resultado;
    }

    private List<ReporteClienteDto> clientes(List<ReporteClienteProjection> filas) {
        List<ReporteClienteDto> resultado = new ArrayList<>();
        for (ReporteClienteProjection f : filas) {
            resultado.add(new ReporteClienteDto(
                    f.getNombre(), valor(f.getEventos()),
                    redondear(valor(f.getFacturado())), redondear(valor(f.getCobrado())),
                    redondear(valor(f.getSaldo()))));
        }
        return resultado;
    }

    private List<ReporteCarteraDto> cartera(List<ReporteCarteraProjection> filas) {
        List<ReporteCarteraDto> resultado = new ArrayList<>();
        for (ReporteCarteraProjection f : filas) {
            resultado.add(new ReporteCarteraDto(
                    f.getCorrelativoPedido(), f.getCliente(), f.getFechaEvento(),
                    redondear(valor(f.getTotal())), redondear(valor(f.getPagado())),
                    redondear(valor(f.getSaldo())), f.getEstadoPago()));
        }
        return resultado;
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    private static double porcentaje(double parte, double total) {
        return total > 0 ? redondear(parte * 100d / total) : 0d;
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100d) / 100d;
    }

    private static long valor(Long valor) {
        return valor != null ? valor : 0L;
    }

    private static double valor(Double valor) {
        return valor != null ? valor : 0d;
    }
}
