package com.canabackend.cana.repositories;

import com.canabackend.cana.models.ServiciosDecoracion;
import com.canabackend.cana.projections.GetServiciosDecoracionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiciosDecoracionRepository extends JpaRepository<ServiciosDecoracion, Long> {
    @Query(value="SELECT COUNT(*) > 0 FROM cana.servicios_decoracion sd \n" +
            "WHERE cana.unaccent(LOWER(sd.nombre_servicio)) = cana.unaccent(LOWER(:nombre))\n" +
            "and sd.estado_servicio is true",nativeQuery=true)
   public Boolean existsByNombre(String nombre);

    @Query(value="SELECT COUNT(*) > 0 FROM cana.servicios_decoracion sd \n" +
            "WHERE cana.unaccent(LOWER(sd.nombre_servicio)) = cana.unaccent(LOWER(:nombre))\n" +
            "AND sd.estado_servicio IS TRUE \n" +
            "AND sd.id_servicio != :idServicio",nativeQuery=true)
    public Boolean existsByNombreEdicion(@Param("nombre") String nombre, @Param("idServicio") Long idServicio);

    @Query(value="select sd.id_servicio as idServicio, sd.id_categoria as idCategoria,\n" +
            "cs.nombre_categoria as nombreCategoria, sd.nombre_servicio as nombreServicio,\n" +
            "sd.descripcion_servicio as descripcionServicio, sd.unidad_medida as unidadMedida,\n" +
            "sd.requiere_detalle as requiereDetalle, sd.estado_servicio as estadoServicio\n" +
            "from cana.servicios_decoracion sd \n" +
            "inner join cana.categorias_servicio cs on\n" +
            "cs.id_categoria = sd.id_categoria\n" +
            "where sd.estado_servicio is true",nativeQuery = true)
    public List<GetServiciosDecoracionProjection> listarServicios();

    boolean existsByIdServicioAndEstadoServicioTrue(Long idServicio);
}
