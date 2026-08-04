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

    @Query(value =
            "select dsc.id_servicio as idServicio, sd.nombre_servicio as nombreServicio, " +
            "dsc.cantidad as cantidad, dsc.precio_cotizado as precioCotizado, " +
            "dsc.especificaciones as especificaciones " +
            "from cana.detalle_servicio_cotizacion dsc " +
            "inner join cana.servicios_decoracion sd on sd.id_servicio = dsc.id_servicio " +
            "where dsc.id_cotizacion = :idCotizacion", nativeQuery = true)
    List<DetalleServicioCotizacionProjection> findDetallesConNombre(@Param("idCotizacion") Long idCotizacion);
}
