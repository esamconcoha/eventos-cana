package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.CrearEntregaDto;
import com.canabackend.cana.dtos.DetalleServicioPedidoListDto;
import com.canabackend.cana.dtos.EntregaDetalleDto;
import com.canabackend.cana.dtos.EntregaListDto;
import com.canabackend.cana.dtos.EstadisticasEntregasDto;
import com.canabackend.cana.dtos.EstadoActualDto;
import com.canabackend.cana.dtos.ItemEntregaDto;
import com.canabackend.cana.dtos.PedidoDisponibleDto;
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
import com.canabackend.cana.projections.DetalleServicioPedidoProjection;
import com.canabackend.cana.projections.EntregaListProjection;
import com.canabackend.cana.projections.EstadisticasProjection;
import com.canabackend.cana.projections.ItemEntregaProjection;
import com.canabackend.cana.projections.PedidoDisponibleProjection;
import com.canabackend.cana.projections.ViajeItemProjection;
import com.canabackend.cana.repositories.DetalleServicioPedidoRepository;
import com.canabackend.cana.repositories.DetalleViajeItemsRepository;
import com.canabackend.cana.repositories.DetalleViajeRepository;
import com.canabackend.cana.repositories.DocumentosEntregaRepository;
import com.canabackend.cana.repositories.EntregasPedidoRepository;
import com.canabackend.cana.repositories.EstadosPedidoRepository;
import com.canabackend.cana.repositories.EstadosRepository;
import com.canabackend.cana.repositories.ItemsCanaRepository;
import com.canabackend.cana.repositories.PedidosCanaRepository;
import com.canabackend.cana.repositories.UsuariosRepository;
import com.canabackend.cana.services.EntregasPedidoSvc;
import com.canabackend.cana.services.PedidosCanaSvc;
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
public class EntregasPedidoSvcImpl implements EntregasPedidoSvc {

    /**
     * Tolerancia al comparar cantidades. cantidad_item es numeric(9,2) pero
     * viaja como double, asi que la comparacion exacta no es confiable.
     */
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
    private ItemsCanaRepository itemsCanaRepository;
    @Autowired
    private DetalleServicioPedidoRepository detalleServicioPedidoRepository;
    @Autowired
    private DocumentosEntregaRepository documentosEntregaRepository;
    @Autowired
    private PedidosCanaSvc pedidosCanaSvc;

    @Override
    @Transactional(readOnly = true)
    public List<EntregaListDto> listarEntregas() {
        List<EntregaListDto> resultado = new ArrayList<>();
        for (EntregaListProjection p : this.entregasPedidoRepository.findEntregasConCliente(MovimientoConstants.TIPO_ENTREGA)) {
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
                        MovimientoConstants.TIPO_ENTREGA, hoy, inicioDelDia, inicioDelDia.plusDays(1));

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
    public EntregaDetalleDto obtenerEntrega(Long idEntrega) {
        return mapToDetalle(buscarEntrega(idEntrega));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDisponibleDto> pedidosDisponibles() {
        List<PedidoDisponibleDto> resultado = new ArrayList<>();
        for (PedidoDisponibleProjection p :
                this.entregasPedidoRepository.findPedidosDisponibles(
                        PedidoConstants.ESTADOS_EVENTO_ADMITEN_ENTREGA, MovimientoConstants.TIPO_ENTREGA)) {
            resultado.add(new PedidoDisponibleDto(
                    p.getCorrelativoPedido(),
                    p.getNombreClientePedido(),
                    p.getDireccionPedido(),
                    p.getFechaEvento(),
                    p.getFechaEntrega(),
                    p.getSalonEntrega(),
                    p.getCodigoEstadoPedido(),
                    p.getNombreEstadoPedido(),
                    p.getTotalItems()));
        }
        return resultado;
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public EntregaDetalleDto crearEntrega(CrearEntregaDto dto) {
        if (dto == null || dto.getCorrelativoPedido() == null) {
            throw new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND);
        }
        if (dto.getCantidadViajesAproximados() == null || dto.getCantidadViajesAproximados() < 1) {
            throw new MSCanaException(ErrorEnum.CANTIDAD_VIAJES_INVALIDA);
        }

        PedidosCana pedido = this.pedidosCanaRepository.findById(dto.getCorrelativoPedido())
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));
        // Antes se validaba una bandera booleana "activo"; ahora estado_pedido
        // es el codigo del ciclo de vida. Solo se abre entrega en los estados
        // que la admiten (fuera quedan ETR/REC/FIN y el cancelado ECA).
        if (!PedidoConstants.ESTADOS_EVENTO_ADMITEN_ENTREGA.contains(pedido.getEstadoPedido())) {
            throw new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND);
        }
        // Un pedido tiene a lo sumo una entrega; es lo que hace que salga del
        // listado de pedidosDisponibles en cuanto se le abre la logistica.
        if (this.entregasPedidoRepository.existsByCorrelativoPedidoAndTipoMovimiento(
                pedido.getCorrelativoPedido(), MovimientoConstants.TIPO_ENTREGA)) {
            throw new MSCanaException(ErrorEnum.ENTREGA_YA_EXISTE);
        }

        EntregasPedido entrega = new EntregasPedido();
        entrega.setCorrelativoPedido(pedido.getCorrelativoPedido());
        entrega.setTipoMovimiento(MovimientoConstants.TIPO_ENTREGA);
        entrega.setCantidadViajesAproximados(dto.getCantidadViajesAproximados());
        entrega.setCantidadViajesReales(0);
        entrega.setPedidoFinalizado(false);
        entrega.setFechaInicioEntrega(null);
        entrega.setFechaFinEntrega(null);
        this.entregasPedidoRepository.save(entrega);

        return mapToDetalle(entrega);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public EntregaDetalleDto registrarViaje(RegistrarViajeDto dto) {
        if (dto == null || dto.getIdEntrega() == null) {
            throw new MSCanaException(ErrorEnum.ENTREGA_NOT_FOUND);
        }
        EntregasPedido entrega = buscarEntrega(dto.getIdEntrega());
        if (Boolean.TRUE.equals(entrega.getPedidoFinalizado())) {
            throw new MSCanaException(ErrorEnum.ENTREGA_YA_FINALIZADA);
        }
        if (CollectionUtils.isEmpty(dto.getItems())) {
            throw new MSCanaException(ErrorEnum.VIAJE_SIN_ITEMS);
        }
        if (dto.getFechaInicioViaje() == null
                || (dto.getFechaFinViaje() != null && dto.getFechaFinViaje().isBefore(dto.getFechaInicioViaje()))) {
            throw new MSCanaException(ErrorEnum.FECHAS_VIAJE_INVALIDAS);
        }

        Map<Long, Double> cantidadPorItem = agruparItemsSolicitados(dto.getItems());
        validarItemsContraPendiente(entrega.getIdEntrega(), cantidadPorItem);

        DetalleViaje viaje = new DetalleViaje();
        viaje.setIdEntrega(entrega.getIdEntrega());
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

        // cantidad_viajes_reales es un contador desnormalizado: el unico lugar
        // que lo mueve es este, en la misma transaccion que crea el viaje.
        int viajesPrevios = entrega.getCantidadViajesReales() != null ? entrega.getCantidadViajesReales() : 0;
        entrega.setCantidadViajesReales(viajesPrevios + 1);
        if (entrega.getFechaInicioEntrega() == null) {
            entrega.setFechaInicioEntrega(dto.getFechaInicioViaje());
        }
        this.entregasPedidoRepository.save(entrega);

        if (viajesPrevios == 0) {
            avanzarEstadoPedido(entrega.getCorrelativoPedido(), PedidoConstants.ESTADO_EVENTO_EN_RUTA_ENTREGA);
        }

        return mapToDetalle(entrega);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public EntregaDetalleDto marcarFinalizada(Long idEntrega) {
        EntregasPedido entrega = buscarEntrega(idEntrega);
        if (Boolean.TRUE.equals(entrega.getPedidoFinalizado())) {
            throw new MSCanaException(ErrorEnum.ENTREGA_YA_FINALIZADA);
        }
        if (this.detalleViajeRepository.countByIdEntrega(idEntrega) == 0) {
            throw new MSCanaException(ErrorEnum.ENTREGA_SIN_VIAJES);
        }

        LocalDateTime ahora = LocalDateTime.now();
        entrega.setPedidoFinalizado(true);
        entrega.setFechaFinEntrega(ahora);
        if (entrega.getFechaInicioEntrega() == null) {
            entrega.setFechaInicioEntrega(ahora);
        }
        this.entregasPedidoRepository.save(entrega);

        avanzarEstadoPedido(entrega.getCorrelativoPedido(), PedidoConstants.ESTADO_EVENTO_ENTREGADO);
        abrirRecoleccion(entrega);

        return mapToDetalle(entrega);
    }

    /**
     * Todo lo que sale vuelve, asi que al cerrar la entrega se abre sola la
     * recoleccion: no hay que acordarse de crearla ni existe un flujo donde
     * no corresponda.
     *
     * La estimacion de viajes de vuelta arranca en los viajes que realmente
     * tomo la ida (si hicieron falta 3 para llevar, es la mejor conjetura para
     * traer). El operador la corrige al programarla.
     *
     * La fecha se deja en null a proposito: nadie sabe todavia cuando se va a
     * recoger, y asi la recoleccion aparece en el contador "sin fecha", que
     * funciona como lista de pendientes por agendar.
     */
    private void abrirRecoleccion(EntregasPedido entrega) {
        if (this.entregasPedidoRepository.existsByCorrelativoPedidoAndTipoMovimiento(
                entrega.getCorrelativoPedido(), MovimientoConstants.TIPO_RECOLECCION)) {
            return;
        }
        int viajesDeIda = entrega.getCantidadViajesReales() != null ? entrega.getCantidadViajesReales() : 1;

        EntregasPedido recoleccion = new EntregasPedido();
        recoleccion.setCorrelativoPedido(entrega.getCorrelativoPedido());
        recoleccion.setTipoMovimiento(MovimientoConstants.TIPO_RECOLECCION);
        recoleccion.setCantidadViajesAproximados(Math.max(viajesDeIda, 1));
        recoleccion.setCantidadViajesReales(0);
        recoleccion.setPedidoFinalizado(false);
        recoleccion.setFechaInicioEntrega(null);
        recoleccion.setFechaFinEntrega(null);
        this.entregasPedidoRepository.save(recoleccion);
    }

    /**
     * El mismo item puede venir repetido en el payload; se suman las cantidades
     * antes de validar, si no cada linea se compararia contra el pendiente
     * completo y entre las dos podrian pasarse del total pedido.
     */
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

    private void validarItemsContraPendiente(Long idEntrega, Map<Long, Double> cantidadPorItem) {
        Map<Long, Double> pendientePorItem = new LinkedHashMap<>();
        for (ItemEntregaProjection p : this.entregasPedidoRepository.findItemsEntrega(idEntrega)) {
            pendientePorItem.put(p.getIdItem(), calcularPendiente(p.getCantidadPedida(), p.getCantidadEnviada()));
        }

        for (Map.Entry<Long, Double> entrada : cantidadPorItem.entrySet()) {
            Long idItem = entrada.getKey();
            if (!this.itemsCanaRepository.existsByIdItemAndEstadoItemTrue(idItem)) {
                throw new MSCanaException(ErrorEnum.ITEM_PEDIDO_NOT_FOUND);
            }
            // El selector de items del viaje sale del detalle del pedido, no del
            // catalogo completo: no se puede despachar algo que no se pidio.
            Double pendiente = pendientePorItem.get(idItem);
            if (pendiente == null) {
                throw new MSCanaException(ErrorEnum.ITEM_NO_PERTENECE_AL_PEDIDO);
            }
            if (entrada.getValue() > pendiente + EPSILON_CANTIDAD) {
                throw new MSCanaException(ErrorEnum.CANTIDAD_ITEM_EXCEDE_PENDIENTE);
            }
        }
    }

    /**
     * Mueve el pedido dentro de su ciclo de vida reutilizando el cambio de
     * estado de PedidosCanaSvc, para que el historial de estados_pedido se
     * arme igual venga de donde venga. No hace nada si ya esta en ese estado.
     */
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

    private EntregasPedido buscarEntrega(Long idEntrega) {
        if (idEntrega == null) {
            throw new MSCanaException(ErrorEnum.ENTREGA_NOT_FOUND);
        }
        return this.entregasPedidoRepository.findById(idEntrega)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.ENTREGA_NOT_FOUND));
    }

    private EntregaDetalleDto mapToDetalle(EntregasPedido entrega) {
        PedidosCana pedido = this.pedidosCanaRepository.findById(entrega.getCorrelativoPedido())
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));

        EntregaDetalleDto dto = new EntregaDetalleDto();
        dto.setIdEntrega(entrega.getIdEntrega());
        dto.setCorrelativoPedido(entrega.getCorrelativoPedido());
        dto.setNombreClientePedido(resolverNombreCliente(pedido));
        dto.setDireccionPedido(pedido.getDireccionPedido());
        dto.setCantidadViajesAproximados(entrega.getCantidadViajesAproximados());
        dto.setCantidadViajesReales(entrega.getCantidadViajesReales() != null ? entrega.getCantidadViajesReales() : 0);
        dto.setPedidoFinalizado(Boolean.TRUE.equals(entrega.getPedidoFinalizado()));
        dto.setFechaInicioEntrega(entrega.getFechaInicioEntrega());
        dto.setFechaFinEntrega(entrega.getFechaFinEntrega());
        dto.setFechaEvento(pedido.getFechaEvento());
        dto.setFechaEntrega(pedido.getFechaEntrega());
        dto.setSalonEntrega(pedido.getSalonEntrega());
        dto.setEstadoActualPedido(resolverEstadoActual(entrega.getCorrelativoPedido()));

        List<ItemEntregaDto> items = new ArrayList<>();
        for (ItemEntregaProjection p :
                this.entregasPedidoRepository.findItemsEntrega(entrega.getIdEntrega())) {
            items.add(new ItemEntregaDto(
                    p.getIdItem(),
                    p.getNombreItem(),
                    p.getCantidadPedida(),
                    p.getCantidadEnviada(),
                    calcularPendiente(p.getCantidadPedida(), p.getCantidadEnviada())));
        }
        dto.setItems(items);

        List<DetalleServicioPedidoListDto> servicios = new ArrayList<>();
        for (DetalleServicioPedidoProjection p :
                this.detalleServicioPedidoRepository.findDetallesConNombre(entrega.getCorrelativoPedido())) {
            servicios.add(new DetalleServicioPedidoListDto(
                    p.getIdServicio(), p.getNombreServicio(), p.getCantidad(), p.getPrecioAcordado(),
                    p.getEspecificaciones(), p.getFechaRealizado(), p.getIdDetalleServPedido()));
        }
        dto.setServicios(servicios);

        dto.setViajes(armarViajes(entrega.getIdEntrega()));
        dto.setConstanciaFirmadaDisponible(
                this.documentosEntregaRepository.existsByIdEntregaAndEstadoRegistroTrue(entrega.getIdEntrega()));
        return dto;
    }

    /**
     * Los items de todos los viajes se traen de una sola vez y se reparten en
     * memoria, para no hacer una consulta por viaje.
     */
    private List<ViajeEntregaDto> armarViajes(Long idEntrega) {
        Map<Long, List<ViajeItemDto>> itemsPorViaje = new LinkedHashMap<>();
        for (ViajeItemProjection p :
                this.detalleViajeItemsRepository.findItemsPorEntrega(idEntrega)) {
            itemsPorViaje.computeIfAbsent(p.getIdViaje(), k -> new ArrayList<>())
                    .add(new ViajeItemDto(p.getIdDetalle(), p.getIdItem(), p.getNombreItem(), p.getCantidadItem()));
        }

        List<ViajeEntregaDto> viajes = new ArrayList<>();
        for (DetalleViaje viaje : this.detalleViajeRepository.findByIdEntregaOrderByFechaInicioViajeAscIdViajeAsc(idEntrega)) {
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

    private double calcularPendiente(Double cantidadPedida, Double cantidadEnviada) {
        double pedida = cantidadPedida != null ? cantidadPedida : 0d;
        double enviada = cantidadEnviada != null ? cantidadEnviada : 0d;
        return Math.max(pedida - enviada, 0d);
    }

    private long valorOCero(Long valor) {
        return valor != null ? valor : 0L;
    }
}
