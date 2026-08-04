package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.*;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.Cotizaciones;
import com.canabackend.cana.models.DetalleCotizacion;
import com.canabackend.cana.models.DetalleServicioCotizacion;
import com.canabackend.cana.projections.DetalleItemCotizacionProjection;
import com.canabackend.cana.projections.DetalleServicioCotizacionProjection;
import com.canabackend.cana.projections.HistorialEstadoCotizacionProjection;
import com.canabackend.cana.repositories.CotizacionesRepository;
import com.canabackend.cana.repositories.DetalleCotizacionRepository;
import com.canabackend.cana.repositories.DetalleServicioCotizacionRepository;
import com.canabackend.cana.repositories.PedidosCanaRepository;
import com.canabackend.cana.services.CotizacionesSvc;
import com.canabackend.cana.services.DocumentosCotizacionSvc;
import com.canabackend.cana.services.PedidosCanaSvc;
import com.canabackend.cana.services.ReporteCotizacionSvc;
import com.canabackend.cana.utils.CotizacionConstants;
import com.canabackend.cana.utils.PedidoConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CotizacionesSvcImpl implements CotizacionesSvc {
    @Autowired
    private CotizacionesRepository repository;
    @Autowired
    private DetalleCotizacionRepository detalleCotizacionRepository;
    @Autowired
    private DetalleServicioCotizacionRepository detalleServicioCotizacionRepository;
    @Autowired
    private ReporteCotizacionSvc reporteCotizacionSvc;
    @Autowired
    private DocumentosCotizacionSvc documentosCotizacionSvc;
    @Autowired
    private PedidosCanaSvc pedidosCanaSvc;
    @Autowired
    private PedidosCanaRepository pedidosCanaRepository;

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public byte[] guardarCotizacion(CrearCotizacionDto cotizacion) {
        if (cotizacion.getFechaHoraEvento() != null
                && cotizacion.getFechaHoraEvento().toLocalDate().isBefore(LocalDate.now())) {
            throw new MSCanaException(ErrorEnum.FECHA_EN_PASADO);
        }
        Cotizaciones cotizacionNueva = new Cotizaciones();
        cotizacionNueva.setNombreClienteCotizacion(cotizacion.getNombreClienteCotizacion());
        cotizacionNueva.setDireccionClienteCotizacion(cotizacion.getDireccionClienteCotizacion());
        cotizacionNueva.setTelefonoClienteCotizacion(cotizacion.getTelefonoClienteCotizacion());
        cotizacionNueva.setFechaCotizacion(LocalDate.now());
        cotizacionNueva.setCotizacionConfirmada(Boolean.TRUE.equals(cotizacion.getCotizacionConfirmada()));
        cotizacionNueva.setDpiNitUsuarioCotizacion(cotizacion.getDpiNitUsuarioCotizacion());
        // El estado "Creada" se retira: toda cotizacion nueva arranca ya en
        // "Pendiente de confirmar" (el estado "C" se inactiva en la BD).
        cotizacionNueva.setEstadoCotizacion(CotizacionConstants.ESTADO_PENDIENTE_CONFIRMAR);
        cotizacionNueva.setFechaHoraEvento(cotizacion.getFechaHoraEvento());
        cotizacionNueva.setCodTipoEvento(cotizacion.getCodTipoEvento());
        this.repository.save(cotizacionNueva);

        if (!CollectionUtils.isEmpty(cotizacion.getDetalleCotizacion())) {
            List<DetalleCotizacion> detalles = new ArrayList<>();
            for (DetalleCotizacionDto detalle : cotizacion.getDetalleCotizacion()) {
                DetalleCotizacion detalleCotizacion = new DetalleCotizacion();
                detalleCotizacion.setIdItem(detalle.getIdItem());
                detalleCotizacion.setCantidadItemCotizacion(detalle.getCantidadItemCotizacion());
                detalleCotizacion.setIdCotizacion(cotizacionNueva.getIdCotizacion());
                detalles.add(detalleCotizacion);
            }
            this.detalleCotizacionRepository.saveAll(detalles);
        }

        if (!CollectionUtils.isEmpty(cotizacion.getDetalleServicioCotizacion())) {
            List<DetalleServicioCotizacion> detallesServicio = new ArrayList<>();
            for (DetalleServicioCotizacionDto detalleServicio : cotizacion.getDetalleServicioCotizacion()) {
                DetalleServicioCotizacion detalleServicioCotizacion = new DetalleServicioCotizacion();
                detalleServicioCotizacion.setIdCotizacion(cotizacionNueva.getIdCotizacion());
                detalleServicioCotizacion.setIdServicio(detalleServicio.getIdServicio());
                detalleServicioCotizacion.setCantidad(detalleServicio.getCantidad());
                detalleServicioCotizacion.setPrecioCotizado(detalleServicio.getPrecioCotizado());
                detalleServicioCotizacion.setEspecificaciones(detalleServicio.getEspecificaciones());
                detallesServicio.add(detalleServicioCotizacion);
            }
            this.detalleServicioCotizacionRepository.saveAll(detallesServicio);
        }

        byte[] pdf = this.reporteCotizacionSvc.generarPdfCotizacion(cotizacionNueva.getIdCotizacion());
        this.documentosCotizacionSvc.guardarDocumentoCotizacion(
                cotizacionNueva.getIdCotizacion(), pdf, cotizacion.getDpiNitUsuarioCotizacion());

        return pdf;
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public byte[] actualizarCotizacion(Long idCotizacion, ActualizarCotizacionDto dto) {
        Cotizaciones cotizacion = this.repository.findById(idCotizacion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.COTIZACION_NOT_FOUND));

        // Confirmada ya genero un pedido con los detalles copiados; cancelada o
        // eliminada no tiene sentido editarlas.
        if (!CotizacionConstants.esEditable(cotizacion.getEstadoCotizacion())) {
            throw new MSCanaException(ErrorEnum.COTIZACION_NO_EDITABLE);
        }
        if (dto.getFechaHoraEvento() != null
                && dto.getFechaHoraEvento().toLocalDate().isBefore(LocalDate.now())) {
            throw new MSCanaException(ErrorEnum.FECHA_EN_PASADO);
        }

        if (dto.getNombreClienteCotizacion() != null) {
            cotizacion.setNombreClienteCotizacion(dto.getNombreClienteCotizacion());
        }
        if (dto.getDireccionClienteCotizacion() != null) {
            cotizacion.setDireccionClienteCotizacion(dto.getDireccionClienteCotizacion());
        }
        if (dto.getTelefonoClienteCotizacion() != null) {
            cotizacion.setTelefonoClienteCotizacion(dto.getTelefonoClienteCotizacion());
        }
        if (dto.getFechaHoraEvento() != null) {
            cotizacion.setFechaHoraEvento(dto.getFechaHoraEvento());
        }
        if (dto.getCodTipoEvento() != null) {
            cotizacion.setCodTipoEvento(dto.getCodTipoEvento());
        }
        this.repository.save(cotizacion);

        // Los detalles se reemplazan por completo, no se mezclan linea por linea.
        if (dto.getDetalleCotizacion() != null) {
            this.detalleCotizacionRepository.deleteByIdCotizacion(idCotizacion);
            guardarDetallesItems(idCotizacion, dto.getDetalleCotizacion());
        }
        if (dto.getDetalleServicioCotizacion() != null) {
            this.detalleServicioCotizacionRepository.deleteByIdCotizacion(idCotizacion);
            guardarDetallesServicios(idCotizacion, dto.getDetalleServicioCotizacion());
        }

        // El PDF se rehace DESPUES de persistir todo: el reporte lee de la base,
        // asi que si se generara antes saldria con los datos viejos.
        byte[] pdf = this.reporteCotizacionSvc.generarPdfCotizacion(idCotizacion);
        this.documentosCotizacionSvc.guardarDocumentoCotizacion(
                idCotizacion, pdf, cotizacion.getDpiNitUsuarioCotizacion());
        return pdf;
    }

    private void guardarDetallesItems(Long idCotizacion, List<DetalleCotizacionDto> detalles) {
        if (CollectionUtils.isEmpty(detalles)) { return; }
        List<DetalleCotizacion> entidades = new ArrayList<>();
        for (DetalleCotizacionDto d : detalles) {
            DetalleCotizacion e = new DetalleCotizacion();
            e.setIdItem(d.getIdItem());
            e.setCantidadItemCotizacion(d.getCantidadItemCotizacion());
            e.setIdCotizacion(idCotizacion);
            entidades.add(e);
        }
        this.detalleCotizacionRepository.saveAll(entidades);
    }

    private void guardarDetallesServicios(Long idCotizacion, List<DetalleServicioCotizacionDto> detalles) {
        if (CollectionUtils.isEmpty(detalles)) { return; }
        List<DetalleServicioCotizacion> entidades = new ArrayList<>();
        for (DetalleServicioCotizacionDto d : detalles) {
            DetalleServicioCotizacion e = new DetalleServicioCotizacion();
            e.setIdCotizacion(idCotizacion);
            e.setIdServicio(d.getIdServicio());
            e.setCantidad(d.getCantidad());
            e.setPrecioCotizado(d.getPrecioCotizado());
            e.setEspecificaciones(d.getEspecificaciones());
            entidades.add(e);
        }
        this.detalleServicioCotizacionRepository.saveAll(entidades);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] obtenerDocumentoCotizacion(Long idCotizacion) {
        return this.documentosCotizacionSvc.obtenerUltimoDocumento(idCotizacion).getContenido();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionListDto> listarCotizaciones() {
        List<Cotizaciones> cotizaciones = this.repository.findAll();
        List<CotizacionListDto> resultado = new ArrayList<>();

        // Una sola consulta: id_cotizacion de las cotizaciones cuyo pedido quedo
        // en estado "Evento cancelado". Sirve para la nota "Pedido cancelado".
        Set<Integer> conPedidoCancelado = new HashSet<>(
                this.pedidosCanaRepository.findIdCotizacionesPorEstadoActual(
                        PedidoConstants.ESTADO_EVENTO_CANCELADO));

        for (Cotizaciones cot : cotizaciones) {
            CotizacionListDto dto = new CotizacionListDto();
            dto.setIdCotizacion(cot.getIdCotizacion());
            dto.setCodigoCotizacion(generarCodigoCotizacion(cot));
            dto.setNombreClienteCotizacion(cot.getNombreClienteCotizacion());
            dto.setDireccionClienteCotizacion(cot.getDireccionClienteCotizacion());
            dto.setTelefonoClienteCotizacion(cot.getTelefonoClienteCotizacion());
            dto.setFechaCotizacion(cot.getFechaCotizacion());
            dto.setFechaHoraEvento(cot.getFechaHoraEvento());
            dto.setEstadoCotizacion(cot.getEstadoCotizacion());
            dto.setDpiNitUsuarioCotizacion(cot.getDpiNitUsuarioCotizacion());
            dto.setCodTipoEvento(cot.getCodTipoEvento());
            dto.setPedidoCancelado(cot.getIdCotizacion() != null
                    && conPedidoCancelado.contains(cot.getIdCotizacion().intValue()));

            List<DetalleItemCotizacionProjection> items =
                    this.detalleCotizacionRepository.findDetallesConNombre(cot.getIdCotizacion());
            List<DetalleItemCotizacionListDto> detallesItems = new ArrayList<>();
            for (DetalleItemCotizacionProjection p : items) {
                detallesItems.add(new DetalleItemCotizacionListDto(
                        p.getIdItem(), p.getNombreItem(), p.getCostoItem(), p.getCantidadItemCotizacion()));
            }
            dto.setDetalles(detallesItems);

            List<DetalleServicioCotizacionProjection> servicios =
                    this.detalleServicioCotizacionRepository.findDetallesConNombre(cot.getIdCotizacion());
            List<DetalleServicioCotizacionListDto> detallesServ = new ArrayList<>();
            for (DetalleServicioCotizacionProjection p : servicios) {
                detallesServ.add(new DetalleServicioCotizacionListDto(
                        p.getIdServicio(), p.getNombreServicio(),
                        p.getCantidad() != null ? p.getCantidad() : 0,
                        p.getPrecioCotizado() != null ? p.getPrecioCotizado() : 0,
                        p.getEspecificaciones()));
            }
            dto.setDetallesServicios(detallesServ);

            resultado.add(dto);
        }
        return resultado;
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void confirmarCotizacion(Long idCotizacion, ConfirmarCotizacionDto confirmacion) {
        Cotizaciones cotizacion = this.repository.findById(idCotizacion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.COTIZACION_NOT_FOUND));
        boolean yaConfirmada = CotizacionConstants.ESTADO_CONFIRMADA.equals(cotizacion.getEstadoCotizacion());

        // Los datos de entrega solo se validan si esta confirmacion va a crear
        // el pedido; re-confirmar una cotizacion ya confirmada no crea nada.
        if (!yaConfirmada) {
            if (confirmacion == null || confirmacion.getFechaEntrega() == null) {
                throw new MSCanaException(ErrorEnum.FECHA_ENTREGA_REQUERIDA);
            }
            if (confirmacion.getFechaEntrega().isBefore(LocalDate.now())) {
                throw new MSCanaException(ErrorEnum.FECHA_EN_PASADO);
            }
            if (confirmacion.getCantidadViajesAproximados() == null
                    || confirmacion.getCantidadViajesAproximados() < 1) {
                throw new MSCanaException(ErrorEnum.CANTIDAD_VIAJES_INVALIDA);
            }
        }

        cotizacion.setEstadoCotizacion(CotizacionConstants.ESTADO_CONFIRMADA);
        cotizacion.setCotizacionConfirmada(true);
        this.repository.save(cotizacion);

        if (!yaConfirmada) {
            this.pedidosCanaSvc.crearPedidoDesdeCotizacion(idCotizacion,
                    confirmacion.getFechaEntrega(), confirmacion.getCantidadViajesAproximados());
        }
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void cancelarCotizacion(Long idCotizacion) {
        Cotizaciones cotizacion = this.repository.findById(idCotizacion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.COTIZACION_NOT_FOUND));
        cotizacion.setEstadoCotizacion(CotizacionConstants.ESTADO_CANCELADA);
        this.repository.save(cotizacion);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void eliminarCotizacion(Long idCotizacion) {
        Cotizaciones cotizacion = this.repository.findById(idCotizacion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.COTIZACION_NOT_FOUND));
        cotizacion.setEstadoCotizacion(CotizacionConstants.ESTADO_ELIMINADA);
        this.repository.save(cotizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoCotizacionDto> historialEstados(Long idCotizacion) {
        this.repository.findById(idCotizacion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.COTIZACION_NOT_FOUND));

        List<HistorialEstadoCotizacionDto> resultado = new ArrayList<>();
        for (HistorialEstadoCotizacionProjection h
                : this.repository.getHistorialEstados(idCotizacion)) {
            resultado.add(new HistorialEstadoCotizacionDto(
                    h.getIdEstado(),
                    h.getCodigoEstado(),
                    h.getNombreEstado(),
                    h.getFechaHoraInicio(),
                    h.getFechaHoraFin()));
        }
        return resultado;
    }

    private String generarCodigoCotizacion(Cotizaciones cot) {
        int year = cot.getFechaCotizacion() != null ? cot.getFechaCotizacion().getYear() : LocalDate.now().getYear();
        return String.format("COT-%d-%04d", year, cot.getIdCotizacion());
    }
}
