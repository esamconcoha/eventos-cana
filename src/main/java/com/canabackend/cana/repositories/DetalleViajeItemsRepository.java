package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DetalleViajeItems;
import com.canabackend.cana.projections.ViajeItemProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleViajeItemsRepository extends JpaRepository<DetalleViajeItems, Long> {

    /**
     * Todos los items de todos los viajes de una entrega en una sola consulta;
     * el servicio los agrupa por viaje. Evita el N+1 de pedir los items viaje
     * por viaje al armar el detalle.
     */
    @Query(value =
            "select dvi.id_detalle as idDetalle, dvi.id_detalle_viaje as idViaje, " +
            "dvi.id_item as idItem, ic.descripcion_item as nombreItem, " +
            "dvi.cantidad_item::double precision as cantidadItem " +
            "from cana.detalle_viaje_items dvi " +
            "inner join cana.detalle_viaje dv on dv.id_viaje = dvi.id_detalle_viaje " +
            "inner join cana.items_cana ic on ic.id_item = dvi.id_item " +
            "where dv.id_entrega = :idEntrega " +
            "order by dvi.id_detalle_viaje, dvi.id_detalle", nativeQuery = true)
    List<ViajeItemProjection> findItemsPorEntrega(@Param("idEntrega") Long idEntrega);
}
