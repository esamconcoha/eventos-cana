package com.canabackend.cana.repositories;

import com.canabackend.cana.models.Cotizaciones;
import com.canabackend.cana.projections.HistorialEstadoCotizacionProjection;
import com.canabackend.cana.projections.ReporteLineaCotizacionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CotizacionesRepository extends JpaRepository<Cotizaciones, Long> {

    /**
     * Devuelve las lineas (articulos + servicios) de una cotizacion unificadas
     * para alimentar el reporte Jasper.
     *
     * <p>El descuento sale resuelto de la BD (monto y etiqueta) para que el PDF
     * muestre exactamente lo mismo que suma el total del pedido cuando la
     * cotizacion se confirme.
     */
    @Query(value =
            "select 'Articulo' as tipo, ic.descripcion_item as descripcion, cast(null as text) as especificaciones, \n" +
            "       dc.cantidad_item_cotizacion as cantidad, ic.costo_item as precioUnitario, \n" +
            "       (dc.cantidad_item_cotizacion * ic.costo_item) as subtotal, \n" +
            "       cana.fn_descuento_linea(dc.cantidad_item_cotizacion * ic.costo_item, \n" +
            "            dc.tipo_descuento, dc.valor_descuento) as montoDescuento, \n" +
            "       cana.fn_etiqueta_descuento(dc.tipo_descuento, dc.valor_descuento, dc.motivo_descuento) as etiquetaDescuento, \n" +
            "       cana.fn_neto_linea(dc.cantidad_item_cotizacion * ic.costo_item, \n" +
            "            dc.tipo_descuento, dc.valor_descuento) as subtotalNeto \n" +
            "from cana.detalle_cotizacion dc \n" +
            "inner join cana.items_cana ic on ic.id_item = dc.id_item \n" +
            "where dc.id_cotizacion = :idCotizacion \n" +
            "union all \n" +
            "select 'Servicio' as tipo, sd.nombre_servicio as descripcion, dsc.especificaciones as especificaciones, \n" +
            "       dsc.cantidad as cantidad, dsc.precio_cotizado as precioUnitario, \n" +
            "       (dsc.cantidad * dsc.precio_cotizado) as subtotal, \n" +
            "       cana.fn_descuento_linea(dsc.cantidad * dsc.precio_cotizado, \n" +
            "            dsc.tipo_descuento, dsc.valor_descuento) as montoDescuento, \n" +
            "       cana.fn_etiqueta_descuento(dsc.tipo_descuento, dsc.valor_descuento, dsc.motivo_descuento) as etiquetaDescuento, \n" +
            "       cana.fn_neto_linea(dsc.cantidad * dsc.precio_cotizado, \n" +
            "            dsc.tipo_descuento, dsc.valor_descuento) as subtotalNeto \n" +
            "from cana.detalle_servicio_cotizacion dsc \n" +
            "inner join cana.servicios_decoracion sd on sd.id_servicio = dsc.id_servicio \n" +
            "where dsc.id_cotizacion = :idCotizacion \n" +
            "order by tipo, descripcion", nativeQuery = true)
    List<ReporteLineaCotizacionProjection> getLineasReporte(@Param("idCotizacion") Long idCotizacion);

    /**
     * Best-effort: obtiene el nombre del tipo de evento a partir de su codigo en
     * el catalogo. Si el codigo se repitiera entre tipos de catalogo, se debe
     * filtrar tambien por id_tipo_catalogo del catalogo de eventos.
     */
    @Query(value = "select cc.nombre from cana.catalogos_cana cc \n" +
            "where cc.codigo = :codTipoEvento \n" +
            "limit 1", nativeQuery = true)
    String getNombreTipoEvento(@Param("codTipoEvento") String codTipoEvento);

    /**
     * Historial de estados de la cotizacion (tabla estados_cotizacion, que
     * mantiene el trigger de sql/008), del mas reciente al mas antiguo.
     *
     * Va por query nativa con join a estados y no por entidad + findById por
     * fila: resuelve el nombre del estado en la misma ida a la BD, en vez del
     * N+1 que haria recorrer el historial resolviendo cada id por separado.
     */
    @Query(value =
            "select e.id_estado as idEstado, e.codigo_estado as codigoEstado, \n" +
            "       e.nombre_estado as nombreEstado, \n" +
            "       ec.fecha_hora_inicio as fechaHoraInicio, ec.fecha_hora_fin as fechaHoraFin \n" +
            "from cana.estados_cotizacion ec \n" +
            "inner join cana.estados e on e.id_estado = ec.id_estado \n" +
            "where ec.id_cotizacion = :idCotizacion \n" +
            "order by ec.fecha_hora_inicio desc", nativeQuery = true)
    List<HistorialEstadoCotizacionProjection> getHistorialEstados(@Param("idCotizacion") Long idCotizacion);
}
