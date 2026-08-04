package com.canabackend.cana.repositories;

import com.canabackend.cana.models.PedidosCana;
import com.canabackend.cana.projections.DashboardResumenProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Repositorio solo-lectura de la pantalla de inicio. Cruza tablas que no
 * pertenecen a un mismo agregado (cotizaciones, items, pedidos, usuarios,
 * movimientos), por eso no cuelga de ningun repositorio de dominio: extiende
 * Repository, el marcador de Spring Data, y no expone CRUD.
 */
public interface DashboardRepository extends Repository<PedidosCana, String> {

    /** Los seis contadores del tablero en una sola ida a la BD. */
    @Query(value =
            "select " +
            " (select count(*) from cana.cotizaciones " +
            "   where estado_cotizacion in ('C', 'P')) as cotizacionesPendientes, " +
            // "stock bajo" no es medible: items_cana no tiene un minimo
            // configurable contra el cual comparar. cantidad_faltantes si es
            // un dato real, y es lo que se cuenta.
            " (select count(*) from cana.items_cana " +
            "   where estado_item is true and coalesce(cantidad_faltantes, 0) > 0) as articulosConFaltantes, " +
            " (select count(*) from cana.pedidos_cana " +
            "   where estado_pedido is distinct from 'ECA' " +
            "     and fecha_evento >= :ahora and fecha_evento < :en7Dias) as eventosProximos7Dias, " +
            " (select count(*) from cana.usuarios where estado_usuario is true) as usuariosActivos, " +
            " (select count(*) from cana.entregas_pedido en " +
            "   inner join cana.pedidos_cana p on p.correlativo_pedido = en.correlativo_pedido " +
            "   where en.tipo_movimiento = 'ENT' " +
            "     and coalesce(en.pedido_finalizado, false) = false " +
            "     and p.estado_pedido is distinct from 'ECA' " +
            "     and p.fecha_entrega = :hoy) as entregasHoy, " +
            " (select count(*) from cana.entregas_pedido en " +
            "   inner join cana.pedidos_cana p on p.correlativo_pedido = en.correlativo_pedido " +
            "   where en.tipo_movimiento = 'REC' " +
            "     and coalesce(en.pedido_finalizado, false) = false " +
            "     and p.estado_pedido is distinct from 'ECA' " +
            "     and p.fecha_recogido is null) as recoleccionesSinAgendar",
            nativeQuery = true)
    DashboardResumenProjection obtenerResumen(@Param("hoy") LocalDate hoy,
                                              @Param("ahora") LocalDateTime ahora,
                                              @Param("en7Dias") LocalDateTime en7Dias);
}
