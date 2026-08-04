package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.ReporteLineaCotizacionDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.Cotizaciones;
import com.canabackend.cana.projections.ReporteLineaCotizacionProjection;
import com.canabackend.cana.repositories.CotizacionesRepository;
import com.canabackend.cana.services.ReporteCotizacionSvc;
import com.canabackend.cana.utils.CotizacionConstants;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteCotizacionSvcImpl implements ReporteCotizacionSvc {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private CotizacionesRepository repository;

    /** Se compila una sola vez y se reutiliza. */
    private volatile JasperReport reporteCompilado;

    @Override
    public byte[] generarPdfCotizacion(Long idCotizacion) {
        Cotizaciones cotizacion = this.repository.findById(idCotizacion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.COTIZACION_NOT_FOUND));

        try {
            JasperReport reporte = obtenerReporteCompilado();

            List<ReporteLineaCotizacionDto> lineas = obtenerLineas(idCotizacion);

            Map<String, Object> parametros = construirParametros(cotizacion);

            try (InputStream logo = new ClassPathResource(CotizacionConstants.REPORTE_LOGO).getInputStream()) {
                parametros.put("LOGO", logo);

                JasperPrint print = JasperFillManager.fillReport(
                        reporte, parametros, new JRBeanCollectionDataSource(lineas));

                return JasperExportManager.exportReportToPdf(print);
            }
        } catch (MSCanaException e) {
            throw e;
        } catch (Exception e) {
            throw new MSCanaException(ErrorEnum.I_ERROR_GENERAR_DOCUMENTO, e);
        }
    }

    private JasperReport obtenerReporteCompilado() throws Exception {
        if (this.reporteCompilado == null) {
            synchronized (this) {
                if (this.reporteCompilado == null) {
                    try (InputStream jrxml = new ClassPathResource(CotizacionConstants.REPORTE_COTIZACION_JRXML).getInputStream()) {
                        this.reporteCompilado = JasperCompileManager.compileReport(jrxml);
                    }
                }
            }
        }
        return this.reporteCompilado;
    }

    private List<ReporteLineaCotizacionDto> obtenerLineas(Long idCotizacion) {
        List<ReporteLineaCotizacionProjection> proyecciones = this.repository.getLineasReporte(idCotizacion);
        List<ReporteLineaCotizacionDto> lineas = new ArrayList<>();
        for (ReporteLineaCotizacionProjection p : proyecciones) {
            lineas.add(new ReporteLineaCotizacionDto(
                    p.getTipo(),
                    p.getDescripcion(),
                    p.getEspecificaciones(),
                    p.getCantidad(),
                    p.getPrecioUnitario(),
                    p.getSubtotal()));
        }
        return lineas;
    }

    private Map<String, Object> construirParametros(Cotizaciones cotizacion) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("NUMERO_COTIZACION", String.format("COT-%06d", cotizacion.getIdCotizacion()));
        parametros.put("FECHA_COTIZACION", cotizacion.getFechaCotizacion() != null
                ? cotizacion.getFechaCotizacion().format(FORMATO_FECHA) : "");
        parametros.put("ESTADO", CotizacionConstants.nombreEstado(cotizacion.getEstadoCotizacion()));
        parametros.put("CLIENTE", nvl(cotizacion.getNombreClienteCotizacion()));
        parametros.put("DIRECCION", nvl(cotizacion.getDireccionClienteCotizacion()));
        parametros.put("TELEFONO", cotizacion.getTelefonoClienteCotizacion() != null
                ? String.valueOf(cotizacion.getTelefonoClienteCotizacion()) : "");
        parametros.put("TIPO_EVENTO", resolverTipoEvento(cotizacion.getCodTipoEvento()));
        parametros.put("FECHA_EVENTO", cotizacion.getFechaHoraEvento() != null
                ? cotizacion.getFechaHoraEvento().format(FORMATO_FECHA_HORA) : "");
        return parametros;
    }

    private String resolverTipoEvento(String codTipoEvento) {
        if (codTipoEvento == null || codTipoEvento.isBlank()) {
            return "";
        }
        String nombre = this.repository.getNombreTipoEvento(codTipoEvento);
        return nombre != null ? nombre : codTipoEvento;
    }

    private String nvl(String valor) {
        return valor != null ? valor : "";
    }
}
