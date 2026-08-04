package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.*;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.*;
import com.canabackend.cana.projections.*;
import com.canabackend.cana.repositories.*;
import com.canabackend.cana.services.PagosPedidoSvc;
import com.canabackend.cana.services.PedidosCanaSvc;
import com.canabackend.cana.utils.MovimientoConstants;
import com.canabackend.cana.utils.PagoConstants;
import com.canabackend.cana.utils.PedidoConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidosCanaSvcImpl implements PedidosCanaSvc {

    @Autowired
    private PedidosCanaRepository pedidosCanaRepository;
    @Autowired
    private DetallePedidoRepository detallePedidoRepository;
    @Autowired
    private DetalleServicioPedidoRepository detalleServicioPedidoRepository;
    @Autowired
    private EstadosPedidoRepository estadosPedidoRepository;
    @Autowired
    private EstadosRepository estadosRepository;
    @Autowired
    private CatalogosCanaRepository catalogosCanaRepository;
    @Autowired
    private UsuariosRepository usuariosRepository;
    @Autowired
    private PagosPedidoSvc pagosPedidoSvc;
    @Autowired
    private CotizacionesRepository cotizacionesRepository;
    @Autowired
    private DetalleCotizacionRepository detalleCotizacionRepository;
    @Autowired
    private DetalleServicioCotizacionRepository detalleServicioCotizacionRepository;
    @Autowired
    private ItemsCanaRepository itemsCanaRepository;
    @Autowired
    private ServiciosDecoracionRepository serviciosDecoracionRepository;
    @Autowired
    private EntregasPedidoRepository entregasPedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDto> listarPedidos() {
        List<PedidoDto> resultado = new ArrayList<>();
        for (PedidosCana pedido : this.pedidosCanaRepository.findAll()) {
            resultado.add(mapToPedidoDto(pedido));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDto obtenerPedido(String correlativoPedido) {
        PedidosCana pedido = this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));
        return mapToPedidoDto(pedido);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public PedidoDto guardarPedido(GuardarPedidoDto dto) {
        validarDatosEntrega(dto.getFechaEntrega(), dto.getCantidadViajesAproximados());
        // Solo al crear: un pedido nuevo no se agenda para ayer. Al editar no
        // se valida, porque hay que poder corregir pedidos historicos.
        validarNoEsPasado(dto.getFechaEntrega());
        validarNoEsPasado(dto.getFechaRecoleccion());
        if (dto.getFechaEvento() != null) {
            validarNoEsPasado(dto.getFechaEvento().toLocalDate());
        }

        PedidosCana pedido = new PedidosCana();
        pedido.setCorrelativoPedido(generarCorrelativoPedido());
        pedido.setNombreClientePedido(dto.getNombreClientePedido());
        pedido.setTelefonoClientePedido(dto.getTelefonoClientePedido());
        pedido.setDireccionPedido(dto.getDireccionPedido());
        pedido.setFechaEvento(dto.getFechaEvento());
        pedido.setSalonEntrega(dto.getSalonEntrega());
        pedido.setFechaEntrega(dto.getFechaEntrega());
        pedido.setFechaRecogido(dto.getFechaRecoleccion());
        pedido.setCodTipoEvento(dto.getCodTipoEvento());
        pedido.setUsuarioInternoPedido(dto.getUsuarioInternoPedido());
        // El estado inicial se persiste en la columna; el trigger sql/008 abre
        // el primer tramo en estados_pedido al INSERT (ya no se crea a mano).
        pedido.setEstadoPedido(PedidoConstants.ESTADO_EVENTO_INICIAL);
        pedido.setPagado(false);
        pedido.setEstadoPago(PagoConstants.ESTADO_PAGO_PENDIENTE);
        this.pedidosCanaRepository.save(pedido);

        validarDetalles(dto.getDetalles(), dto.getDetallesServicios());
        guardarDetalles(pedido.getCorrelativoPedido(), dto.getDetalles(), dto.getDetallesServicios());
        crearEntregaInicial(pedido.getCorrelativoPedido(), dto.getCantidadViajesAproximados());

        return mapToPedidoDto(pedido);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public PedidoDto actualizarPedido(String correlativoPedido, ActualizarPedidoDto dto) {
        PedidosCana pedido = this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));

        if (dto.getDireccionPedido() != null) {
            pedido.setDireccionPedido(dto.getDireccionPedido());
        }
        if (dto.getSalonEntrega() != null) {
            pedido.setSalonEntrega(dto.getSalonEntrega());
        }
        if (dto.getFechaEntrega() != null) {
            pedido.setFechaEntrega(dto.getFechaEntrega());
        }
        if (dto.getFechaRecoleccion() != null) {
            pedido.setFechaRecogido(dto.getFechaRecoleccion());
        }
        if (dto.getFechaEvento() != null) {
            pedido.setFechaEvento(dto.getFechaEvento());
        }
        this.pedidosCanaRepository.save(pedido);

        boolean reemplazaDetalles = dto.getDetalles() != null || dto.getDetallesServicios() != null;
        if (reemplazaDetalles) {
            validarDetalles(dto.getDetalles(), dto.getDetallesServicios());
            if (dto.getDetalles() != null) {
                this.detallePedidoRepository.deleteByCorrelativoPedido(correlativoPedido);
            }
            if (dto.getDetallesServicios() != null) {
                this.detalleServicioPedidoRepository.deleteByCorrelativoPedido(correlativoPedido);
            }
            guardarDetalles(correlativoPedido, dto.getDetalles(), dto.getDetallesServicios());
            // Los items/servicios cambian montoTotalPedido; se recalcula estado_pago/pagado
            // contra los pagos activos ya registrados, sin tocarlos (sube o baja el estado segun corresponda).
            this.pagosPedidoSvc.recalcularEstadoPago(correlativoPedido);
        }

        return mapToPedidoDto(pedido);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public PedidoDto cambiarEstado(String correlativoPedido, Long idEstado) {
        PedidosCana pedido = this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));
        Estados estado = this.estadosRepository.findById(idEstado)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.ESTADO_PEDIDO_NOT_FOUND));

        // Solo se actualiza el codigo del estado actual en la tabla maestra.
        // El cierre del tramo abierto y la apertura del nuevo en estados_pedido
        // los hace el trigger trg_trazabilidad_estado_pedido (sql/008).
        pedido.setEstadoPedido(estado.getCodigoEstado());
        this.pedidosCanaRepository.save(pedido);

        return mapToPedidoDto(pedido);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public PedidoDto cancelarPedido(String correlativoPedido) {
        PedidosCana pedido = this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));

        Estados cancelado = this.estadosRepository
                .findByTipoEstadoAndCodigoEstado(PedidoConstants.TIPO_ESTADO_EVENTO, PedidoConstants.ESTADO_EVENTO_CANCELADO)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.ESTADO_CANCELADO_NO_CONFIGURADO));

        // No re-cancelar: duplicaria el tramo ECA en el historial. El estado
        // actual ahora se lee directo de la columna (la mantiene el trigger).
        if (PedidoConstants.ESTADO_EVENTO_CANCELADO.equals(pedido.getEstadoPedido())) {
            throw new MSCanaException(ErrorEnum.PEDIDO_YA_CANCELADO);
        }

        // Reutiliza cambiarEstado: cierra el tramo abierto y abre el de ECA, asi
        // el historial de estados_pedido se arma igual que cualquier otro cambio.
        return cambiarEstado(correlativoPedido, cancelado.getIdEstado());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoPedidoDto> historialEstados(String correlativoPedido) {
        this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));

        List<HistorialEstadoPedidoDto> resultado = new ArrayList<>();
        for (EstadosPedido h : this.estadosPedidoRepository.findByCorrelativoPedidoOrderByFechaHoraInicioDesc(correlativoPedido)) {
            Estados estado = this.estadosRepository.findById(h.getIdEstado()).orElse(null);
            resultado.add(new HistorialEstadoPedidoDto(
                    h.getIdEstado(),
                    estado != null ? estado.getCodigoEstado() : null,
                    estado != null ? estado.getNombreEstado() : null,
                    h.getFechaHoraInicio(),
                    h.getFechaHoraFin()));
        }
        return resultado;
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void crearPedidoDesdeCotizacion(Long idCotizacion, LocalDate fechaEntrega,
                                            Integer cantidadViajesAproximados) {
        if (this.pedidosCanaRepository.existsByIdCotizacion(idCotizacion.intValue())) {
            return;
        }
        validarDatosEntrega(fechaEntrega, cantidadViajesAproximados);

        Cotizaciones cotizacion = this.cotizacionesRepository.findById(idCotizacion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.COTIZACION_NOT_FOUND));

        PedidosCana pedido = new PedidosCana();
        pedido.setCorrelativoPedido(generarCorrelativoPedido());
        pedido.setNombreClientePedido(cotizacion.getNombreClienteCotizacion());
        Long telefono = cotizacion.getTelefonoClienteCotizacion();
        pedido.setTelefonoClientePedido(telefono != null ? telefono.intValue() : null);
        pedido.setDireccionPedido(cotizacion.getDireccionClienteCotizacion());
        pedido.setFechaEvento(cotizacion.getFechaHoraEvento());
        // La cotizacion no tiene fecha de entrega: se captura al confirmarla.
        pedido.setFechaEntrega(fechaEntrega);
        pedido.setCodTipoEvento(cotizacion.getCodTipoEvento());
        pedido.setUsuarioInternoPedido(cotizacion.getDpiNitUsuarioCotizacion());
        pedido.setIdCotizacion(idCotizacion.intValue());
        pedido.setEstadoPedido(PedidoConstants.ESTADO_EVENTO_INICIAL);
        pedido.setPagado(false);
        pedido.setEstadoPago(PagoConstants.ESTADO_PAGO_PENDIENTE);
        this.pedidosCanaRepository.save(pedido);

        List<DetalleCotizacion> detallesCotizacion = this.detalleCotizacionRepository.findByIdCotizacion(idCotizacion);
        if (!CollectionUtils.isEmpty(detallesCotizacion)) {
            List<DetallePedido> detalles = new ArrayList<>();
            for (DetalleCotizacion d : detallesCotizacion) {
                DetallePedido detalle = new DetallePedido();
                detalle.setIdItem(d.getIdItem());
                detalle.setCantidadItemPedido(d.getCantidadItemCotizacion());
                detalle.setCorrelativoPedido(pedido.getCorrelativoPedido());
                detalles.add(detalle);
            }
            this.detallePedidoRepository.saveAll(detalles);
        }

        List<DetalleServicioCotizacion> detallesServicioCotizacion =
                this.detalleServicioCotizacionRepository.findByIdCotizacion(idCotizacion);
        if (!CollectionUtils.isEmpty(detallesServicioCotizacion)) {
            List<DetalleServicioPedido> detallesServicio = new ArrayList<>();
            for (DetalleServicioCotizacion d : detallesServicioCotizacion) {
                DetalleServicioPedido detalle = new DetalleServicioPedido();
                detalle.setIdServicio(d.getIdServicio());
                detalle.setCantidad(d.getCantidad());
                detalle.setPrecioAcordado(d.getPrecioCotizado());
                detalle.setEspecificaciones(d.getEspecificaciones());
                detalle.setCorrelativoPedido(pedido.getCorrelativoPedido());
                detallesServicio.add(detalle);
            }
            this.detalleServicioPedidoRepository.saveAll(detallesServicio);
        }

        // El primer tramo de estados_pedido lo abre el trigger sql/008 al INSERT.
        crearEntregaInicial(pedido.getCorrelativoPedido(), cantidadViajesAproximados);
    }

    /** Rechaza fechas anteriores a hoy. Ignora null: la obligatoriedad se valida aparte. */
    private void validarNoEsPasado(LocalDate fecha) {
        if (fecha != null && fecha.isBefore(LocalDate.now())) {
            throw new MSCanaException(ErrorEnum.FECHA_EN_PASADO);
        }
    }

    private void validarDatosEntrega(LocalDate fechaEntrega, Integer cantidadViajesAproximados) {
        if (fechaEntrega == null) {
            throw new MSCanaException(ErrorEnum.FECHA_ENTREGA_REQUERIDA);
        }
        if (cantidadViajesAproximados == null || cantidadViajesAproximados < 1) {
            throw new MSCanaException(ErrorEnum.CANTIDAD_VIAJES_INVALIDA);
        }
    }

    /**
     * Todo pedido nace con su entrega abierta, asi que la logistica no depende
     * de que alguien se acuerde de crearla despues.
     *
     * Se usa el repositorio y no EntregasPedidoSvc a proposito: ese servicio ya
     * depende de PedidosCanaSvc para mover estados, y inyectarlo aca cerraria
     * un ciclo que Spring Boot rechaza al arrancar.
     */
    private void crearEntregaInicial(String correlativoPedido, Integer cantidadViajesAproximados) {
        EntregasPedido entrega = new EntregasPedido();
        entrega.setCorrelativoPedido(correlativoPedido);
        // Obligatorio aunque la columna tenga DEFAULT: Hibernate incluye la
        // columna en el INSERT, asi que un null aca llega como null a la BD.
        entrega.setTipoMovimiento(MovimientoConstants.TIPO_ENTREGA);
        entrega.setCantidadViajesAproximados(cantidadViajesAproximados);
        entrega.setCantidadViajesReales(0);
        entrega.setPedidoFinalizado(false);
        entrega.setFechaInicioEntrega(null);
        entrega.setFechaFinEntrega(null);
        this.entregasPedidoRepository.save(entrega);
    }

    private void validarDetalles(List<DetallePedidoDto> detalles, List<DetalleServicioPedidoDto> detallesServicios) {
        if (!CollectionUtils.isEmpty(detalles)) {
            for (DetallePedidoDto d : detalles) {
                if (!this.itemsCanaRepository.existsByIdItemAndEstadoItemTrue(d.getIdItem())) {
                    throw new MSCanaException(ErrorEnum.ITEM_PEDIDO_NOT_FOUND);
                }
            }
        }
        if (!CollectionUtils.isEmpty(detallesServicios)) {
            for (DetalleServicioPedidoDto d : detallesServicios) {
                if (!this.serviciosDecoracionRepository.existsByIdServicioAndEstadoServicioTrue(d.getIdServicio())) {
                    throw new MSCanaException(ErrorEnum.SERVICIO_PEDIDO_NOT_FOUND);
                }
            }
        }
    }

    private void guardarDetalles(String correlativoPedido, List<DetallePedidoDto> detalles,
                                  List<DetalleServicioPedidoDto> detallesServicios) {
        if (!CollectionUtils.isEmpty(detalles)) {
            List<DetallePedido> entidades = new ArrayList<>();
            for (DetallePedidoDto d : detalles) {
                DetallePedido detalle = new DetallePedido();
                detalle.setIdItem(d.getIdItem());
                detalle.setCantidadItemPedido(d.getCantidadItemPedido());
                detalle.setCorrelativoPedido(correlativoPedido);
                entidades.add(detalle);
            }
            this.detallePedidoRepository.saveAll(entidades);
        }
        if (!CollectionUtils.isEmpty(detallesServicios)) {
            List<DetalleServicioPedido> entidades = new ArrayList<>();
            for (DetalleServicioPedidoDto d : detallesServicios) {
                DetalleServicioPedido detalle = new DetalleServicioPedido();
                detalle.setIdServicio(d.getIdServicio());
                detalle.setCantidad(d.getCantidad());
                detalle.setPrecioAcordado(d.getPrecioAcordado());
                detalle.setEspecificaciones(d.getEspecificaciones());
                detalle.setCorrelativoPedido(correlativoPedido);
                entidades.add(detalle);
            }
            this.detalleServicioPedidoRepository.saveAll(entidades);
        }
    }

    private String generarCorrelativoPedido() {
        int year = LocalDate.now().getYear();
        String prefijo = PedidoConstants.PREFIJO_CORRELATIVO_PEDIDO + year + "-";
        long siguiente = this.pedidosCanaRepository.countByCorrelativoPedidoStartingWith(prefijo) + 1;
        return prefijo + String.format("%05d", siguiente);
    }

    private PedidoDto mapToPedidoDto(PedidosCana pedido) {
        PedidoDto dto = new PedidoDto();
        dto.setCorrelativoPedido(pedido.getCorrelativoPedido());
        dto.setNombreClientePedido(resolverNombreCliente(pedido));
        dto.setTelefonoClientePedido(pedido.getTelefonoClientePedido());
        dto.setDireccionPedido(pedido.getDireccionPedido());
        dto.setFechaEvento(pedido.getFechaEvento());
        dto.setFechaEntrega(pedido.getFechaEntrega());
        dto.setSalonEntrega(pedido.getSalonEntrega());
        dto.setFechaConfirmacionPedido(pedido.getFechaConfirmacionPedido());
        dto.setFechaRecoleccion(pedido.getFechaRecogido());
        dto.setPagado(pedido.getPagado());
        dto.setEstadoPago(pedido.getEstadoPago());
        dto.setUsuarioInternoPedido(pedido.getUsuarioInternoPedido());
        dto.setCodTipoEvento(pedido.getCodTipoEvento());
        dto.setNombreTipoEvento(this.catalogosCanaRepository.getNombreByCodigo(pedido.getCodTipoEvento()));
        dto.setIdCotizacion(pedido.getIdCotizacion());

        this.estadosPedidoRepository.findFirstByCorrelativoPedidoAndFechaHoraFinIsNull(pedido.getCorrelativoPedido())
                .ifPresent(historico -> dto.setEstadoActual(resolverEstadoActual(historico.getIdEstado())));

        EstadoCuentaPedidoDto estadoCuenta = this.pagosPedidoSvc.obtenerEstadoCuenta(pedido.getCorrelativoPedido());
        dto.setMontoTotalPedido(estadoCuenta.getTotalPedido());
        dto.setSaldoPendiente(estadoCuenta.getSaldoPendiente());

        List<DetallePedidoListDto> detalles = new ArrayList<>();
        for (DetalleItemPedidoProjection p :
                this.detallePedidoRepository.findDetallesConNombre(pedido.getCorrelativoPedido())) {
            detalles.add(new DetallePedidoListDto(p.getIdItem(), p.getNombreItem(), p.getCostoItem(), p.getCantidadItemPedido()));
        }
        dto.setDetalles(detalles);

        List<DetalleServicioPedidoListDto> detallesServicios = new ArrayList<>();
        for (DetalleServicioPedidoProjection p :
                this.detalleServicioPedidoRepository.findDetallesConNombre(pedido.getCorrelativoPedido())) {
            detallesServicios.add(new DetalleServicioPedidoListDto(
                    p.getIdServicio(), p.getNombreServicio(), p.getCantidad(), p.getPrecioAcordado(),
                    p.getEspecificaciones(), p.getFechaRealizado(), p.getIdDetalleServPedido()));
        }
        dto.setDetallesServicios(detallesServicios);

        return dto;
    }

    private String resolverNombreCliente(PedidosCana pedido) {
        if (pedido.getDpiUsuarioPedido() != null) {
            return this.usuariosRepository.findById(pedido.getDpiUsuarioPedido())
                    .map(u -> u.getNombresUsuario() + " " + u.getApellidosUsuario())
                    .orElse(pedido.getNombreClientePedido());
        }
        return pedido.getNombreClientePedido();
    }

    private EstadoActualDto resolverEstadoActual(Long idEstado) {
        return this.estadosRepository.findById(idEstado)
                .map(e -> new EstadoActualDto(e.getIdEstado(), e.getCodigoEstado(), e.getNombreEstado()))
                .orElse(null);
    }
}
