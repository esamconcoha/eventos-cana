package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DetalleServicioCotizacion;
import com.canabackend.cana.projections.DetalleServicioCotizacionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleServicioCotizacionRepository extends JpaRepository<DetalleServicioCotizacion, Long> {

    List<DetalleServicioCotizacion> findByIdCotizacion(Long idCotizacion);

    void deleteByIdCotizacion(Long idCotizacion);

    /** Misma formula de descuento que el total del pedido; ver DetalleCotizacionRepository. */
    @Query(value =
            "select dsc.id_servicio as idServicio, sd.nombre_servicio as nombreServicio, " +
            "dsc.cantidad as cantidad, dsc.precio_cotizado as precioCotizado, " +
            "dsc.especificaciones as especificaciones, " +
            "dsc.tipo_descuento as tipoDescuento, " +
            "cast(dsc.valor_descuento as double precision) as valorDescuento, " +
            "dsc.motivo_descuento as motivoDescuento, " +
            "cast(cana.fn_descuento_linea(dsc.cantidad * dsc.precio_cotizado, " +
            "     dsc.tipo_descuento, dsc.valor_descuento) as double precision) as montoDescuento, " +
            "cast(cana.fn_neto_linea(dsc.cantidad * dsc.precio_cotizado, " +
            "     dsc.tipo_descuento, dsc.valor_descuento) as double precision) as subtotalNeto " +
            "from cana.detalle_servicio_cotizacion dsc " +
            "inner join cana.servicios_decoracion sd on sd.id_servicio = dsc.id_servicio " +
            "where dsc.id_cotizacion = :idCotizacion", nativeQuery = true)
    List<DetalleServicioCotizacionProjection> findDetallesConNombre(@Param("idCotizacion") Long idCotizacion);
}
