package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.DetalleServicioPedidoListDto;
import com.canabackend.cana.dtos.EntregaDetalleDto;
import com.canabackend.cana.dtos.ItemEntregaDto;
import com.canabackend.cana.dtos.ReporteLineaEntregaDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.services.EntregasPedidoSvc;
import com.canabackend.cana.services.ReporteEntregaSvc;
import com.canabackend.cana.utils.EntregaReporteConstants;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteEntregaSvcImpl implements ReporteEntregaSvc {

    private static final double EPSILON_CANTIDAD = 0.0001d;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private EntregasPedidoSvc entregasPedidoSvc;

    /** Se compila una sola vez y se reutiliza (mismo criterio que la cotizacion). */
    private volatile JasperReport reporteCompilado;

    @Override
    public byte[] generarPdfEntrega(Long idEntrega) {
        // obtenerEntrega ya valida la existencia (ENTREGA_NOT_FOUND) y arma items,
        // servicios y viajes: reusarlo evita duplicar las consultas de logistica.
        EntregaDetalleDto entrega = this.entregasPedidoSvc.obtenerEntrega(idEntrega);

        try {
            JasperReport reporte = obtenerReporteCompilado();

            List<ReporteLineaEntregaDto> lineas = construirLineas(entrega);

            Map<String, Object> parametros = construirParametros(entrega);

            try (InputStream logo = new ClassPathResource(EntregaReporteConstants.REPORTE_LOGO).getInputStream()) {
                parametros.put("LOGO", logo);

                JasperPrint print = JasperFillManager.fillReport(
                        reporte, parametros, new JRBeanCollectionDataSource(lineas));

                return JasperExportManager.exportReportToPdf(print);
            }
        } catch (MSCanaException e) {
            throw e;
        } catch (Exception e) {
            throw new MSCanaException(ErrorEnum.I_ERROR_GENERAR_CONSTANCIA, e);
        }
    }

    private JasperReport obtenerReporteCompilado() throws Exception {
        if (this.reporteCompilado == null) {
            synchronized (this) {
                if (this.reporteCompilado == null) {
                    try (InputStream jrxml = new ClassPathResource(EntregaReporteConstants.REPORTE_ENTREGA_JRXML).getInputStream()) {
                        this.reporteCompilado = JasperCompileManager.compileReport(jrxml);
                    }
                }
            }
        }
        return this.reporteCompilado;
    }

    /**
     * La constancia deja constancia de lo que YA salio: articulos con cantidad
     * enviada > 0 y los servicios contratados (que se realizan en el mismo
     * movimiento). Lo pendiente no va: aun no se entrego.
     */
    private List<ReporteLineaEntregaDto> construirLineas(EntregaDetalleDto entrega) {
        List<ReporteLineaEntregaDto> lineas = new ArrayList<>();

        if (entrega.getItems() != null) {
            for (ItemEntregaDto item : entrega.getItems()) {
                double enviado = item.getCantidadEnviada() != null ? item.getCantidadEnviada() : 0d;
                if (enviado <= EPSILON_CANTIDAD) {
                    continue;
                }
                lineas.add(new ReporteLineaEntregaDto(
                        EntregaReporteConstants.TIPO_ARTICULO,
                        item.getNombreItem(),
                        null,
                        enviado));
            }
        }

        if (entrega.getServicios() != null) {
            for (DetalleServicioPedidoListDto servicio : entrega.getServicios()) {
                lineas.add(new ReporteLineaEntregaDto(
                        EntregaReporteConstants.TIPO_SERVICIO,
                        servicio.getNombreServicio(),
                        servicio.getEspecificaciones(),
                        servicio.getCantidad()));
            }
        }

        return lineas;
    }

    private Map<String, Object> construirParametros(EntregaDetalleDto entrega) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("NUMERO_ENTREGA", nvl(entrega.getCorrelativoPedido()));
        parametros.put("CLIENTE", nvl(entrega.getNombreClientePedido()));
        parametros.put("DIRECCION", nvl(entrega.getDireccionPedido()));
        parametros.put("FECHA_ENTREGA", entrega.getFechaEntrega() != null
                ? entrega.getFechaEntrega().format(FORMATO_FECHA) : "");
        parametros.put("FECHA_EMISION", LocalDateTime.now().format(FORMATO_FECHA_HORA));
        parametros.put("ESTADO", Boolean.TRUE.equals(entrega.getPedidoFinalizado())
                ? EntregaReporteConstants.ESTADO_FINALIZADA
                : EntregaReporteConstants.ESTADO_EN_CURSO);
        return parametros;
    }

    private String nvl(String valor) {
        return valor != null ? valor : "";
    }
}
