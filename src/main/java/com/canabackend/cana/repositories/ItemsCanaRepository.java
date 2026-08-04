package com.canabackend.cana.repositories;

import com.canabackend.cana.dtos.GetItemsExistentesDto;
import com.canabackend.cana.models.EstadosPedido;
import com.canabackend.cana.models.ItemsCana;
import com.canabackend.cana.projections.views.GetItemsExistentesProjectionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemsCanaRepository extends JpaRepository<ItemsCana,Long> {
    @Query(value="select ic.id_item as idItem, ic.id_tipo_item as idTipoItem,ic.costo_item as costoItem,\n" +
            "ic.descripcion_item as descripcionItem,ic.cantidad_item as cantidadItem, ic.observaciones as observaciones,\n" +
            "ic.cantidad_faltantes as cantidadFaltante,cc.nombre as nombreItem,cc.estado_registro as estadoItem\n" +
            "from cana.items_cana ic \n" +
            "inner join cana.catalogos_cana cc on\n" +
            "cc.id_catalogo= ic.id_tipo_item and cc.id_tipo_catalogo=2\n" +
            "where ic.estado_item is true",nativeQuery = true)
    public List<GetItemsExistentesProjectionView> getItemsExistentes();


    @Query(value = "SELECT COUNT(*) > 0 FROM cana.items_cana ic \n" +
            "WHERE cana.unaccent(LOWER(ic.descripcion_item)) = cana.unaccent(LOWER(:descripcion))\n" +
            "and ic.estado_item is true",
            nativeQuery = true)
    boolean existeDescripcion(@Param("descripcion") String descripcion);

    @Query(value = "SELECT COUNT(*) > 0 FROM cana.items_cana ic \n" +
            "WHERE cana.unaccent(LOWER(ic.descripcion_item)) = cana.unaccent(LOWER(:descripcion))\n" +
            "AND ic.estado_item IS TRUE \n" +
            "AND ic.id_item != :idItem",
            nativeQuery = true)
    boolean existeDescripcionEdicion(@Param("descripcion") String descripcion,
                                     @Param("idItem") Long idItem);

    boolean existsByIdItemAndEstadoItemTrue(Long idItem);
}
