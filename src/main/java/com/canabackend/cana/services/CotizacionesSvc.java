package com.canabackend.cana.services;

import com.canabackend.cana.dtos.ConfirmarCotizacionDto;
import com.canabackend.cana.dtos.CotizacionListDto;
import com.canabackend.cana.dtos.ActualizarCotizacionDto;
import com.canabackend.cana.dtos.CrearCotizacionDto;
import com.canabackend.cana.dtos.HistorialEstadoCotizacionDto;

import java.util.List;

public interface CotizacionesSvc {

    byte[] guardarCotizacion(CrearCotizacionDto cotizacion);

    /**
     * Modifica una cotizacion aun no confirmada y REGENERA su documento.
     *
     * El PDF nuevo se guarda como una version mas en documentos_cotizacion,
     * sin borrar las anteriores: obtenerUltimoDocumento ya devuelve el mas
     * reciente, asi que la descarga toma los cambios sola y queda el rastro
     * de lo que se le habia enviado antes al cliente.
     *
     * @return el PDF actualizado
     */
    byte[] actualizarCotizacion(Long idCotizacion, ActualizarCotizacionDto cotizacion);

    byte[] obtenerDocumentoCotizacion(Long idCotizacion);

    List<CotizacionListDto> listarCotizaciones();

    /**
     * Confirma la cotizacion y, la primera vez, crea el pedido con su entrega
     * ya abierta usando la fecha y la estimacion de viajes de la confirmacion.
     */
    void confirmarCotizacion(Long idCotizacion, ConfirmarCotizacionDto confirmacion);

    void cancelarCotizacion(Long idCotizacion);

    void eliminarCotizacion(Long idCotizacion);

    /**
     * Historial de estados por los que paso la cotizacion, del mas reciente al
     * mas antiguo. Lo alimenta el trigger de trazabilidad (sql/008).
     */
    List<HistorialEstadoCotizacionDto> historialEstados(Long idCotizacion);
}
