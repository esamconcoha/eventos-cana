package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DetalleCotizacion;
import com.canabackend.cana.projections.DetalleItemCotizacionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCotizacionRepository extends JpaRepository<DetalleCotizacion, Long> {

    List<DetalleCotizacion> findByIdCotizacion(Long idCotizacion);

    void deleteByIdCotizacion(Long idCotizacion);

    @Query(value =
            "select dc.id_item as idItem, ic.descripcion_item as nombreItem, " +
            "ic.costo_item as costoItem, dc.cantidad_item_cotizacion as cantidadItemCotizacion " +
            "from cana.detalle_cotizacion dc " +
            "inner join cana.items_cana ic on ic.id_item = dc.id_item " +
            "where dc.id_cotizacion = :idCotizacion", nativeQuery = true)
    List<DetalleItemCotizacionProjection> findDetallesConNombre(@Param("idCotizacion") Long idCotizacion);
}
