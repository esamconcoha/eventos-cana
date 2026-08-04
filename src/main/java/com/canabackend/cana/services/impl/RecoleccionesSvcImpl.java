package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.EntregaListDto;
import com.canabackend.cana.dtos.EstadisticasEntregasDto;
import com.canabackend.cana.dtos.EstadoActualDto;
import com.canabackend.cana.dtos.ItemRecoleccionDto;
import com.canabackend.cana.dtos.ProgramarRecoleccionDto;
import com.canabackend.cana.dtos.RecoleccionDetalleDto;
import com.canabackend.cana.dtos.RegistrarViajeDto;
import com.canabackend.cana.dtos.ViajeEntregaDto;
import com.canabackend.cana.dtos.ViajeItemDto;
import com.canabackend.cana.dtos.ViajeItemRequestDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.DetalleViaje;
import com.canabackend.cana.models.DetalleViajeItems;
import com.canabackend.cana.models.EntregasPedido;
import com.canabackend.cana.models.Estados;
import com.canabackend.cana.models.EstadosPedido;
import com.canabackend.cana.models.PedidosCana;
import com.canabackend.cana.projections.EntregaListProjection;
import com.canabackend.cana.projections.EstadisticasProjection;
import com.canabackend.cana.projections.ItemRecoleccionProjection;
import com.canabackend.cana.projections.ViajeItemProjection;
import com.canabackend.cana.repositories.DetalleViajeItemsRepository;
import com.canabackend.cana.repositories.DetalleViajeRepository;
import com.canabackend.cana.repositories.EntregasPedidoRepository;
import com.canabackend.cana.repositories.EstadosPedidoRepository;
import com.canabackend.cana.repositories.EstadosRepository;
import com.canabackend.cana.repositories.PedidosCanaRepository;
import com.canabackend.cana.repositories.UsuariosRepository;
import com.canabackend.cana.services.PedidosCanaSvc;
import com.canabackend.cana.services.RecoleccionesSvc;
import com.canabackend.cana.utils.MovimientoConstants;
import com.canabackend.cana.utils.PedidoConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecoleccionesSvcImpl implements RecoleccionesSvc {

    /** Misma tolerancia que en entregas: cantidad_item es numeric leido como double. */
    private static final double EPSILON_CANTIDAD = 0.0001d;

    @Autowired
    private EntregasPedidoRepository entregasPedidoRepository;
    @Autowired
    private DetalleViajeRepository detalleViajeRepository;
    @Autowired
    private DetalleViajeItemsRepository detalleViajeItemsRepository;
    @Autowired
    private PedidosCanaRepository pedidosCanaRepository;
    @Autowired
    private EstadosPedidoRepository estadosPedidoRepository;
    @Autowired
    private EstadosRepository estadosRepository;
    @Autowired
    private UsuariosRepository usuariosRepository;
    @Autowired
    private PedidosCanaSvc pedidosCanaSvc;

    @Override
    @Transactional(readOnly = true)
    public List<EntregaListDto> listarRecolecciones() {
        List<EntregaListDto> resultado = new ArrayList<>();
        for (EntregaListProjection p :
                this.entregasPedidoRepository.findEntregasConCliente(MovimientoConstants.TIPO_RECOLECCION)) {
            EntregaListDto dto = new EntregaListDto();
            dto.setIdEntrega(p.getIdEntrega());
            dto.setCorrelativoPedido(p.getCorrelativoPedido());
            dto.setNombreClientePedido(p.getNombreClientePedido());
            dto.setDireccionPedido(p.getDireccionPedido());
            dto.setCantidadViajesAproximados(p.getCantidadViajesAproximados());
            dto.setCantidadViajesReales(p.getCantidadViajesReales());
            dto.setPedidoFinalizado(p.getPedidoFinalizado());
            dto.setFechaInicioEntrega(p.getFechaInicioEntrega());
            dto.setFechaFinEntrega(p.getFechaFinEntrega());
            dto.setFechaEvento(p.getFechaEvento());
            // Para una fila 'REC' la consulta ya devuelve fecha_recogido aca.
            dto.setFechaEntrega(p.getFechaEntrega());
            if (p.getIdEstadoPedido() != null) {
                dto.setEstadoActualPedido(new EstadoActualDto(
                        p.getIdEstadoPedido(), p.getCodigoEstadoPedido(), p.getNombreEstadoPedido()));
            }
            resultado.add(dto);
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasEntregasDto obtenerEstadisticas() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        EstadisticasProjection p =
                this.entregasPedidoRepository.obtenerEstadisticas(
                        MovimientoConstants.TIPO_RECOLECCION, hoy, inicioDelDia, inicioDelDia.plusDays(1));

        if (p == null) {
            return new EstadisticasEntregasDto(0L, 0L, 0L, 0L, 0L, 0L, 0L);
        }
        return new EstadisticasEntregasDto(
                valorOCero(p.getEnCurso()),
                valorOCero(p.getProgramadas()),
                valorOCero(p.getAtrasadas()),
                valorOCero(p.getSinFecha()),
                valorOCero(p.getFinalizadas()),
                valorOCero(p.getViajesHoy()),
                valorOCero(p.getDesvioViajes()));
    }

    @Override
    @Transactional(readOnly = true)
    public RecoleccionDetalleDto obtenerRecoleccion(Long idRecoleccion) {
        return mapToDetalle(buscarRecoleccion(idRecoleccion));
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public RecoleccionDetalleDto programar(Long idRecoleccion, ProgramarRecoleccionDto datos) {
        EntregasPedido recoleccion = buscarRecoleccion(idRecoleccion);
        if (Boolean.TRUE.equals(recoleccion.getPedidoFinalizado())) {
            throw new MSCanaException(ErrorEnum.RECOLECCION_YA_FINALIZADA);
        }
        if (datos == null || datos.getFechaRecoleccion() == null) {
            throw new MSCanaException(ErrorEnum.FECHA_RECOLECCION_REQUERIDA);
        }
        if (datos.getCantidadViajesAproximados() != null && datos.getCantidadViajesAproximados() < 1) {
            throw new MSCanaException(ErrorEnum.CANTIDAD_VIAJES_INVALIDA);
        }

        // La fecha agendada vive en el pedido, igual que fecha_entrega para la ida.
        PedidosCana pedido = buscarPedido(recoleccion.getCorrelativoPedido());
        pedido.setFechaRecogido(datos.getFechaRecoleccion());
        this.pedidosCanaRepository.save(pedido);

        if (datos.getCantidadViajesAproximados() != null) {
            recoleccion.setCantidadViajesAproximados(datos.getCantidadViajesAproximados());
            this.entregasPedidoRepository.save(recoleccion);
        }
        return mapToDetalle(recoleccion);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public RecoleccionDetalleDto registrarViaje(RegistrarViajeDto dto) {
        if (dto == null || dto.getIdEntrega() == null) {
            throw new MSCanaException(ErrorEnum.RECOLECCION_NOT_FOUND);
        }
        EntregasPedido recoleccion = buscarRecoleccion(dto.getIdEntrega());
        if (Boolean.TRUE.equals(recoleccion.getPedidoFinalizado())) {
            throw new MSCanaException(ErrorEnum.RECOLECCION_YA_FINALIZADA);
        }
        if (CollectionUtils.isEmpty(dto.getItems())) {
            throw new MSCanaException(ErrorEnum.VIAJE_SIN_ITEMS);
        }
        if (dto.getFechaInicioViaje() == null
                || (dto.getFechaFinViaje() != null && dto.getFechaFinViaje().isBefore(dto.getFechaInicioViaje()))) {
            throw new MSCanaException(ErrorEnum.FECHAS_VIAJE_INVALIDAS);
        }

        Map<Long, Double> cantidadPorItem = agruparItemsSolicitados(dto.getItems());
        validarContraLoEntregado(recoleccion.getIdEntrega(), cantidadPorItem);

        DetalleViaje viaje = new DetalleViaje();
        viaje.setIdEntrega(recoleccion.getIdEntrega());
        viaje.setFechaInicioViaje(dto.getFechaInicioViaje());
        viaje.setFechaFinViaje(dto.getFechaFinViaje());
        this.detalleViajeRepository.save(viaje);

        List<DetalleViajeItems> items = new ArrayList<>();
        for (Map.Entry<Long, Double> entrada : cantidadPorItem.entrySet()) {
            DetalleViajeItems item = new DetalleViajeItems();
            item.setIdDetalleViaje(viaje.getIdViaje());
            item.setIdItem(entrada.getKey());
            item.setCantidadItem(entrada.getValue());
            items.add(item);
        }
        this.detalleViajeItemsRepository.saveAll(items);

        int viajesPrevios = recoleccion.getCantidadViajesReales() != null
                ? recoleccion.getCantidadViajesReales() : 0;
        recoleccion.setCantidadViajesReales(viajesPrevios + 1);
        if (recoleccion.getFechaInicioEntrega() == null) {
            recoleccion.setFechaInicioEntrega(dto.getFechaInicioViaje());
        }
        this.entregasPedidoRepository.save(recoleccion);

        if (viajesPrevios == 0) {
            avanzarEstadoPedido(recoleccion.getCorrelativoPedido(), PedidoConstants.ESTADO_EVENTO_RECOLECTADO);
        }
        return mapToDetalle(recoleccion);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public RecoleccionDetalleDto marcarFinalizada(Long idRecoleccion) {
        EntregasPedido recoleccion = buscarRecoleccion(idRecoleccion);
        if (Boolean.TRUE.equals(recoleccion.getPedidoFinalizado())) {
            throw new MSCanaException(ErrorEnum.RECOLECCION_YA_FINALIZADA);
        }
        if (this.detalleViajeRepository.countByIdEntrega(idRecoleccion) == 0) {
            throw new MSCanaException(ErrorEnum.RECOLECCION_SIN_VIAJES);
        }

        // No se exige que haya vuelto todo: lo que quede pendiente es el
        // faltante del pedido, y esa es informacion valida de cerrar.
        LocalDateTime ahora = LocalDateTime.now();
        recoleccion.setPedidoFinalizado(true);
        recoleccion.setFechaFinEntrega(ahora);
        if (recoleccion.getFechaInicioEntrega() == null) {
            recoleccion.setFechaInicioEntrega(ahora);
        }
        this.entregasPedidoRepository.save(recoleccion);

        // Si nadie la agendo, se sella con el dia en que efectivamente cerro.
        PedidosCana pedido = buscarPedido(recoleccion.getCorrelativoPedido());
        if (pedido.getFechaRecogido() == null) {
            pedido.setFechaRecogido(ahora.toLocalDate());
            this.pedidosCanaRepository.save(pedido);
        }

        avanzarEstadoPedido(recoleccion.getCorrelativoPedido(), PedidoConstants.ESTADO_EVENTO_FINALIZADO);
        return mapToDetalle(recoleccion);
    }

    // ─── Validacion propia de la vuelta ───────────────────────────────

    private Map<Long, Double> agruparItemsSolicitados(List<ViajeItemRequestDto> solicitados) {
        Map<Long, Double> cantidadPorItem = new LinkedHashMap<>();
        for (ViajeItemRequestDto item : solicitados) {
            if (item == null || item.getIdItem() == null) {
                throw new MSCanaException(ErrorEnum.ITEM_PEDIDO_NOT_FOUND);
            }
            if (item.getCantidadItem() == null || item.getCantidadItem() <= 0d) {
                throw new MSCanaException(ErrorEnum.CANTIDAD_ITEM_VIAJE_INVALIDA);
            }
            cantidadPorItem.merge(item.getIdItem(), item.getCantidadItem(), Double::sum);
        }
        return cantidadPorItem;
    }

    /**
     * Aca esta la diferencia de fondo con la entrega: el tope no sale de
     * detalle_pedido sino de lo que realmente salio en los viajes de ida. No se
     * valida que el item este activo en el catalogo: si salio de bodega tiene
     * que poder volver, aunque despues lo hayan dado de baja.
     */
    private void validarContraLoEntregado(Long idRecoleccion, Map<Long, Double> cantidadPorItem) {
        Map<Long, Double> pendientePorItem = new LinkedHashMap<>();
        for (ItemRecoleccionProjection p :
                this.entregasPedidoRepository.findItemsRecoleccion(idRecoleccion)) {
            pendientePorItem.put(p.getIdItem(),
                    calcularPendiente(p.getCantidadEntregada(), p.getCantidadRecolectada()));
        }

        for (Map.Entry<Long, Double> entrada : cantidadPorItem.entrySet()) {
            Double pendiente = pendientePorItem.get(entrada.getKey());
            if (pendiente == null) {
                throw new MSCanaException(ErrorEnum.ITEM_NO_FUE_ENTREGADO);
            }
            if (entrada.getValue() > pendiente + EPSILON_CANTIDAD) {
                throw new MSCanaException(ErrorEnum.CANTIDAD_ITEM_EXCEDE_ENTREGADO);
            }
        }
    }

    // ─── Auxiliares ───────────────────────────────────────────────────

    private void avanzarEstadoPedido(String correlativoPedido, String codigoEstado) {
        EstadoActualDto estadoActual = resolverEstadoActual(correlativoPedido);
        if (estadoActual != null && codigoEstado.equals(estadoActual.getCodigoEstado())) {
            return;
        }
        Estados destino = this.estadosRepository
                .findByTipoEstadoAndCodigoEstado(PedidoConstants.TIPO_ESTADO_EVENTO, codigoEstado)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.ESTADO_ENTREGA_NO_CONFIGURADO));
        this.pedidosCanaSvc.cambiarEstado(correlativoPedido, destino.getIdEstado());
    }

    private EntregasPedido buscarRecoleccion(Long idRecoleccion) {
        if (idRecoleccion == null) {
            throw new MSCanaException(ErrorEnum.RECOLECCION_NOT_FOUND);
        }
        EntregasPedido movimiento = this.entregasPedidoRepository.findById(idRecoleccion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.RECOLECCION_NOT_FOUND));
        // Un id de entrega no es un id de recoleccion valido, aunque ambos
        // vivan en la misma tabla.
        if (!MovimientoConstants.TIPO_RECOLECCION.equals(movimiento.getTipoMovimiento())) {
            throw new MSCanaException(ErrorEnum.RECOLECCION_NOT_FOUND);
        }
        return movimiento;
    }

    private PedidosCana buscarPedido(String correlativoPedido) {
        return this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));
    }

    private RecoleccionDetalleDto mapToDetalle(EntregasPedido recoleccion) {
        PedidosCana pedido = buscarPedido(recoleccion.getCorrelativoPedido());

        RecoleccionDetalleDto dto = new RecoleccionDetalleDto();
        dto.setIdRecoleccion(recoleccion.getIdEntrega());
        dto.setCorrelativoPedido(recoleccion.getCorrelativoPedido());
        dto.setNombreClientePedido(resolverNombreCliente(pedido));
        dto.setDireccionPedido(pedido.getDireccionPedido());
        dto.setCantidadViajesAproximados(recoleccion.getCantidadViajesAproximados());
        dto.setCantidadViajesReales(recoleccion.getCantidadViajesReales() != null
                ? recoleccion.getCantidadViajesReales() : 0);
        dto.setRecoleccionFinalizada(Boolean.TRUE.equals(recoleccion.getPedidoFinalizado()));
        dto.setFechaInicioRecoleccion(recoleccion.getFechaInicioEntrega());
        dto.setFechaFinRecoleccion(recoleccion.getFechaFinEntrega());
        dto.setFechaEvento(pedido.getFechaEvento());
        dto.setFechaRecoleccion(pedido.getFechaRecogido());
        dto.setSalonEntrega(pedido.getSalonEntrega());
        dto.setEstadoActualPedido(resolverEstadoActual(recoleccion.getCorrelativoPedido()));

        List<ItemRecoleccionDto> items = new ArrayList<>();
        double totalPendiente = 0d;
        int itemsConPendiente = 0;
        for (ItemRecoleccionProjection p :
                this.entregasPedidoRepository.findItemsRecoleccion(recoleccion.getIdEntrega())) {
            double pendiente = calcularPendiente(p.getCantidadEntregada(), p.getCantidadRecolectada());
            items.add(new ItemRecoleccionDto(
                    p.getIdItem(), p.getNombreItem(),
                    p.getCantidadEntregada(), p.getCantidadRecolectada(), pendiente));
            if (pendiente > EPSILON_CANTIDAD) {
                totalPendiente += pendiente;
                itemsConPendiente++;
            }
        }
        dto.setItems(items);
        dto.setTotalPendiente(totalPendiente);
        dto.setItemsConPendiente(itemsConPendiente);
        dto.setViajes(armarViajes(recoleccion.getIdEntrega()));
        return dto;
    }

    private List<ViajeEntregaDto> armarViajes(Long idRecoleccion) {
        Map<Long, List<ViajeItemDto>> itemsPorViaje = new LinkedHashMap<>();
        for (ViajeItemProjection p :
                this.detalleViajeItemsRepository.findItemsPorEntrega(idRecoleccion)) {
            itemsPorViaje.computeIfAbsent(p.getIdViaje(), k -> new ArrayList<>())
                    .add(new ViajeItemDto(p.getIdDetalle(), p.getIdItem(), p.getNombreItem(), p.getCantidadItem()));
        }

        List<ViajeEntregaDto> viajes = new ArrayList<>();
        for (DetalleViaje viaje :
                this.detalleViajeRepository.findByIdEntregaOrderByFechaInicioViajeAscIdViajeAsc(idRecoleccion)) {
            ViajeEntregaDto dto = new ViajeEntregaDto();
            dto.setIdViaje(viaje.getIdViaje());
            dto.setFechaInicioViaje(viaje.getFechaInicioViaje());
            dto.setFechaFinViaje(viaje.getFechaFinViaje());
            dto.setItems(itemsPorViaje.getOrDefault(viaje.getIdViaje(), new ArrayList<>()));
            viajes.add(dto);
        }
        return viajes;
    }

    private EstadoActualDto resolverEstadoActual(String correlativoPedido) {
        EstadosPedido historico = this.estadosPedidoRepository
                .findFirstByCorrelativoPedidoAndFechaHoraFinIsNull(correlativoPedido)
                .orElse(null);
        if (historico == null) {
            return null;
        }
        return this.estadosRepository.findById(historico.getIdEstado())
                .map(e -> new EstadoActualDto(e.getIdEstado(), e.getCodigoEstado(), e.getNombreEstado()))
                .orElse(null);
    }

    private String resolverNombreCliente(PedidosCana pedido) {
        if (pedido.getDpiUsuarioPedido() != null) {
            return this.usuariosRepository.findById(pedido.getDpiUsuarioPedido())
                    .map(u -> u.getNombresUsuario() + " " + u.getApellidosUsuario())
                    .orElse(pedido.getNombreClientePedido());
        }
        return pedido.getNombreClientePedido();
    }

    private double calcularPendiente(Double entregada, Double recolectada) {
        double sale = entregada != null ? entregada : 0d;
        double vuelve = recolectada != null ? recolectada : 0d;
        return Math.max(sale - vuelve, 0d);
    }

    private long valorOCero(Long valor) {
        return valor != null ? valor : 0L;
    }
}
