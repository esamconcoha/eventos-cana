package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.ReporteCarteraDto;
import com.canabackend.cana.dtos.ReporteClienteDto;
import com.canabackend.cana.dtos.ReporteDistribucionDto;
import com.canabackend.cana.dtos.ReporteFilaPdfDto;
import com.canabackend.cana.dtos.ReporteGeneralDto;
import com.canabackend.cana.dtos.ReporteRankingDto;
import com.canabackend.cana.dtos.ReporteResumenDto;
import com.canabackend.cana.dtos.ReporteSerieMensualDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.services.ReporteEstadisticoSvc;
import com.canabackend.cana.services.ReportesSvc;
import com.canabackend.cana.utils.GraficoUtil;
import com.canabackend.cana.utils.ReporteConstants;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReporteEstadisticoSvcImpl implements ReporteEstadisticoSvc {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Simbolo de la moneda local; el sistema no maneja mas de una. */
    private static final String MONEDA = "Q";

    private static final String SECCION_ARTICULOS = "Detalle de artículos más rentados";
    private static final String SECCION_SERVICIOS = "Servicios más vendidos";
    private static final String SECCION_CLIENTES = "Clientes con mayor facturación";
    private static final String SECCION_CARTERA = "Pedidos con saldo pendiente";
    private static final String SECCION_FALTANTES = "Faltantes de inventario (estado actual de bodega)";

    @Autowired
    private ReportesSvc reportesSvc;

    /** Se compila una sola vez y se reutiliza (mismo criterio que cotizacion y entrega). */
    private volatile JasperReport reporteCompilado;

    @Override
    public byte[] generarPdf(LocalDate desde, LocalDate hasta) {
        // obtenerReporteGeneral ya valida el rango: el PDF y la pantalla salen
        // del mismo calculo, no de dos consultas que podrian divergir.
        ReporteGeneralDto reporte = this.reportesSvc.obtenerReporteGeneral(desde, hasta);

        try {
            JasperReport plantilla = obtenerReporteCompilado();
            Map<String, Object> parametros = construirParametros(reporte);
            List<ReporteFilaPdfDto> filas = construirFilas(reporte);

            try (InputStream logo = new ClassPathResource(ReporteConstants.REPORTE_LOGO).getInputStream()) {
                parametros.put("LOGO", logo);

                JasperPrint print = JasperFillManager.fillReport(
                        plantilla, parametros, new JRBeanCollectionDataSource(filas));

                return JasperExportManager.exportReportToPdf(print);
            }
        } catch (MSCanaException e) {
            throw e;
        } catch (Exception e) {
            throw new MSCanaException(ErrorEnum.I_ERROR_GENERAR_REPORTE, e);
        }
    }

    private JasperReport obtenerReporteCompilado() throws Exception {
        if (this.reporteCompilado == null) {
            synchronized (this) {
                if (this.reporteCompilado == null) {
                    try (InputStream jrxml =
                                 new ClassPathResource(ReporteConstants.REPORTE_ESTADISTICO_JRXML).getInputStream()) {
                        this.reporteCompilado = JasperCompileManager.compileReport(jrxml);
                    }
                }
            }
        }
        return this.reporteCompilado;
    }

    // ─── Cabecera, indicadores y graficas ────────────────────────

    private Map<String, Object> construirParametros(ReporteGeneralDto reporte) {
        ReporteResumenDto r = reporte.getResumen();
        Map<String, Object> p = new HashMap<>();

        p.put("PERIODO", reporte.getDesde().format(FORMATO_FECHA) + " al " + reporte.getHasta().format(FORMATO_FECHA));
        p.put("FECHA_EMISION", LocalDateTime.now().format(FORMATO_FECHA_HORA));

        kpi(p, 1, "EVENTOS DEL PERIODO", entero(r.getEventos()),
                r.getEventosFinalizados() + " finalizados · " + r.getEventosCancelados() + " cancelados");
        kpi(p, 2, "FACTURADO", moneda(r.getFacturado()), "Artículos y servicios de los pedidos");
        kpi(p, 3, "COBRADO", moneda(r.getCobrado()), porcentaje(r.getPorcentajeCobrado()) + " de lo facturado");
        kpi(p, 4, "SALDO POR COBRAR", moneda(r.getSaldoPorCobrar()), r.getPedidosConSaldo() + " pedidos con saldo");
        kpi(p, 5, "TICKET PROMEDIO", moneda(r.getTicketPromedio()), "Por evento no cancelado");
        kpi(p, 6, "CONVERSIÓN DE COTIZACIONES", porcentaje(r.getTasaConversion()),
                r.getCotizacionesConfirmadas() + " de " + r.getCotizaciones() + " cotizaciones");
        kpi(p, 7, "TASA DE CANCELACIÓN", porcentaje(r.getTasaCancelacion()),
                r.getEventosCancelados() + " eventos cancelados");
        kpi(p, 8, "FALTANTES EN BODEGA", entero(r.getUnidadesFaltantes()),
                r.getArticulosConFaltantes() + " artículos afectados");

        p.put("GRAFICO_INGRESOS", graficoIngresos(reporte.getSerieMensual()));
        p.put("GRAFICO_TIPOS", graficoDona(reporte.getEventosPorTipo(), false));
        p.put("GRAFICO_CARTERA", graficoDona(reporte.getCarteraPorEstadoPago(), true));
        p.put("TITULO_ARTICULOS", "Artículos más rentados (unidades)");
        p.put("GRAFICO_ARTICULOS", graficoArticulos(reporte.getTopArticulos()));
        return p;
    }

    private void kpi(Map<String, Object> parametros, int indice, String label, String valor, String nota) {
        parametros.put("K" + indice + "_LABEL", label);
        parametros.put("K" + indice + "_VALOR", valor);
        parametros.put("K" + indice + "_NOTA", nota);
    }

    private java.awt.Image graficoIngresos(List<ReporteSerieMensualDto> serie) {
        List<String> etiquetas = new ArrayList<>();
        List<Double> facturado = new ArrayList<>();
        List<Double> cobrado = new ArrayList<>();
        for (ReporteSerieMensualDto mes : serie) {
            etiquetas.add(mes.getEtiqueta());
            facturado.add(mes.getFacturado());
            cobrado.add(mes.getCobrado());
        }
        return GraficoUtil.barrasAgrupadas(etiquetas,
                facturado, "Facturado", GraficoUtil.AZUL,
                cobrado, "Cobrado", GraficoUtil.AMBAR,
                545, 150);
    }

    /**
     * @param porMonto la dona de cartera reparte dinero; la de tipos, cantidad
     *                 de eventos.
     */
    private java.awt.Image graficoDona(List<ReporteDistribucionDto> distribucion, boolean porMonto) {
        List<String> etiquetas = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        for (ReporteDistribucionDto d : distribucion) {
            double valor = porMonto ? nvl(d.getMonto()) : (d.getCantidad() != null ? d.getCantidad() : 0d);
            // Un estado de pago sin saldo (PAGADO) no aporta a la dona de cartera
            // y ensuciaria la leyenda con una porcion invisible.
            if (valor <= 0) {
                continue;
            }
            etiquetas.add(d.getEtiqueta());
            valores.add(valor);
        }
        return GraficoUtil.dona(etiquetas, valores, 265, 135);
    }

    private java.awt.Image graficoArticulos(List<ReporteRankingDto> articulos) {
        List<String> etiquetas = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        for (ReporteRankingDto a : articulos) {
            etiquetas.add(a.getEtiqueta());
            valores.add(nvl(a.getCantidad()));
        }
        return GraficoUtil.barrasHorizontales(etiquetas, valores, GraficoUtil.AZUL, false, 545, 145);
    }

    // ─── Tablas ──────────────────────────────────────────────────

    private List<ReporteFilaPdfDto> construirFilas(ReporteGeneralDto reporte) {
        List<ReporteFilaPdfDto> filas = new ArrayList<>();

        for (ReporteRankingDto a : reporte.getTopArticulos()) {
            filas.add(fila(SECCION_ARTICULOS, "Artículo", "Categoría", "Unidades", "Eventos", "Facturado",
                    a.getEtiqueta(), a.getCategoria(),
                    numero(a.getCantidad()), entero(a.getEventos()), moneda(a.getMonto())));
        }

        for (ReporteRankingDto s : reporte.getTopServicios()) {
            filas.add(fila(SECCION_SERVICIOS, "Servicio", "Categoría", "Cantidad", "Eventos", "Facturado",
                    s.getEtiqueta(), s.getCategoria(),
                    numero(s.getCantidad()), entero(s.getEventos()), moneda(s.getMonto())));
        }

        for (ReporteClienteDto c : reporte.getTopClientes()) {
            filas.add(fila(SECCION_CLIENTES, "Cliente", "", "Eventos", "Cobrado", "Facturado",
                    c.getNombre(), "",
                    entero(c.getEventos()), moneda(c.getCobrado()), moneda(c.getFacturado())));
        }

        for (ReporteCarteraDto c : reporte.getPedidosConSaldo()) {
            filas.add(fila(SECCION_CARTERA, "Pedido", "Cliente", "Evento", "Total", "Saldo",
                    c.getCorrelativoPedido(), c.getCliente(),
                    c.getFechaEvento() != null ? c.getFechaEvento().format(FORMATO_FECHA) : "",
                    moneda(c.getTotal()), moneda(c.getSaldo())));
        }

        for (ReporteRankingDto f : reporte.getArticulosConFaltantes()) {
            filas.add(fila(SECCION_FALTANTES, "Artículo", "Categoría", "Faltantes", "En bodega", "Costo",
                    f.getEtiqueta(), f.getCategoria(),
                    numero(f.getCantidad()), entero(f.getDisponible()), moneda(f.getMonto())));
        }

        return filas;
    }

    private ReporteFilaPdfDto fila(String seccion, String e1, String e2, String e3, String e4, String e5,
                                   String titulo, String subtitulo, String v1, String v2, String v3) {
        return new ReporteFilaPdfDto(seccion, e1, e2, e3, e4, e5, nvl(titulo), nvl(subtitulo), v1, v2, v3);
    }

    // ─── Formato ─────────────────────────────────────────────────

    private static String moneda(Double valor) {
        return MONEDA + " " + String.format(Locale.US, "%,.2f", nvl(valor));
    }

    private static String numero(Double valor) {
        double v = nvl(valor);
        return v == Math.rint(v)
                ? String.format(Locale.US, "%,d", (long) v)
                : String.format(Locale.US, "%,.2f", v);
    }

    private static String entero(Long valor) {
        return valor != null ? String.format(Locale.US, "%,d", valor) : "—";
    }

    private static String porcentaje(Double valor) {
        return String.format(Locale.US, "%.1f%%", nvl(valor));
    }

    private static double nvl(Double valor) {
        return valor != null ? valor : 0d;
    }

    private static String nvl(String valor) {
        return valor != null ? valor : "";
    }
}
