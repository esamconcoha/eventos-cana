package com.canabackend.cana.repositories;

import com.canabackend.cana.models.PedidosCana;
import com.canabackend.cana.projections.EventoCalendarioProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidosCanaRepository extends JpaRepository<PedidosCana,String> {

    long countByCorrelativoPedidoStartingWith(String prefijo);

    boolean existsByIdCotizacion(Integer idCotizacion);

    /**
     * id_cotizacion de los pedidos cuyo estado ACTUAL (tramo abierto en
     * estados_pedido) tiene el codigo indicado. Se usa para marcar en el listado
     * de cotizaciones cuales tienen su pedido cancelado, en una sola consulta.
     */
    @Query(value =
            "select p.id_cotizacion from cana.pedidos_cana p " +
            "join cana.estados_pedido ep on ep.correlativo_pedido = p.correlativo_pedido " +
            "     and ep.fecha_hora_fin is null " +
            "join cana.estados e on e.id_estado = ep.id_estado " +
            "where p.id_cotizacion is not null and e.codigo_estado = :codigoEstado", nativeQuery = true)
    List<Integer> findIdCotizacionesPorEstadoActual(@Param("codigoEstado") String codigoEstado);

    /**
     * Eventos del calendario: la fecha del evento en si, con su hora.
     *
     * El nombre del tipo de evento sale por subconsulta con limit 1 y no por
     * join, igual que getNombreByCodigo: catalogos_cana.codigo no es unico
     * entre tipos de catalogo y un join duplicaria filas del calendario.
     */
    @Query(value =
            "select p.correlativo_pedido as correlativoPedido, " +
            "p.fecha_evento as fechaHora, " +
            "coalesce(u.nombres_usuario || ' ' || u.apellidos_usuario, p.nombre_cliente_pedidoprov) as nombreCliente, " +
            "p.direccion_pedido as ubicacion, " +
            "(select cc.nombre from cana.catalogos_cana cc " +
            "  where cc.codigo = p.cod_tipo_evento limit 1) as nombreTipoEvento, " +
            "e.codigo_estado as codigoEstadoPedido, e.nombre_estado as nombreEstadoPedido " +
            "from cana.pedidos_cana p " +
            "left join cana.usuarios u on u.dpi_nit_usuario = p.dpi_usuario_pedido " +
            "left join cana.estados_pedido ep on ep.correlativo_pedido = p.correlativo_pedido " +
            "     and ep.fecha_hora_fin is null " +
            "left join cana.estados e on e.id_estado = ep.id_estado " +
            "where p.estado_pedido is distinct from 'ECA' " +
            "  and p.fecha_evento >= :desde and p.fecha_evento < :hasta " +
            "order by p.fecha_evento", nativeQuery = true)
    List<EventoCalendarioProjection> findEventosEntre(@Param("desde") LocalDateTime desde,
                                                      @Param("hasta") LocalDateTime hasta);
}
