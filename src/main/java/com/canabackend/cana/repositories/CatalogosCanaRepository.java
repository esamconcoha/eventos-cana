package com.canabackend.cana.repositories;

import com.canabackend.cana.models.CatalogosCana;
import com.canabackend.cana.projections.GetCatalogoByNombreProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogosCanaRepository extends JpaRepository<CatalogosCana, Long> {

    @Query(value="select cc.nombre from cana.catalogos_cana cc \n" +
            "where cc.id_catalogo=:idCatalogo", nativeQuery = true)
    public String getNombreCatalogo(@Param("idCatalogo") Long idCatalogo);


    @Query(value="select cc.id_catalogo as idCatalogo,cc.codigo as codigo,cc.nombre as nombre from cana.catalogos_cana cc \n" +
            "inner join cana.tipo_catalogos_cana tcc on\n" +
            "cc.id_tipo_catalogo = tcc.id_tipo_catalogo\n" +
            "where tcc.nombre_catalogo=:nombreTipoCatalogo",nativeQuery = true)
    public List<GetCatalogoByNombreProjection> getCatalogoByNombre(@Param("nombreTipoCatalogo") String nombreTipoCatalogo);

    @Query(value = "select cc.nombre from cana.catalogos_cana cc \n" +
            "where cc.codigo = :codigo \n" +
            "limit 1", nativeQuery = true)
    String getNombreByCodigo(@Param("codigo") String codigo);

    @Query(value = "select cc.id_catalogo as idCatalogo, cc.codigo as codigo, cc.nombre as nombre " +
            "from cana.catalogos_cana cc where cc.id_tipo_catalogo = :idTipoCatalogo", nativeQuery = true)
    List<GetCatalogoByNombreProjection> getCatalogoByIdTipoCatalogo(@Param("idTipoCatalogo") Long idTipoCatalogo);

    /**
     * usuarios.rol es FK a catalogos_cana.id_catalogo: los roles son un catalogo
     * mas. Devuelve el codigo (estable, es sobre lo que se deciden permisos)
     * junto al nombre (solo para mostrar).
     */
    @Query(value = "select cc.id_catalogo as idCatalogo, cc.codigo as codigo, cc.nombre as nombre " +
            "from cana.catalogos_cana cc where cc.id_catalogo = :idCatalogo", nativeQuery = true)
    GetCatalogoByNombreProjection getCatalogoPorId(@Param("idCatalogo") Long idCatalogo);
}
