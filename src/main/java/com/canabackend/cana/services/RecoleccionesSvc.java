package com.canabackend.cana.services;

import com.canabackend.cana.dtos.EntregaListDto;
import com.canabackend.cana.dtos.EstadisticasEntregasDto;
import com.canabackend.cana.dtos.ProgramarRecoleccionDto;
import com.canabackend.cana.dtos.RecoleccionDetalleDto;
import com.canabackend.cana.dtos.RegistrarViajeDto;

import java.util.List;

/**
 * La vuelta: lo que regresa a bodega despues del evento.
 *
 * Comparte tabla y estructura con la entrega (entregas_pedido con
 * tipo_movimiento = 'REC', mas detalle_viaje / detalle_viaje_items), pero la
 * regla de negocio central es distinta: el tope de lo que se puede recolectar
 * es lo que efectivamente SALIO, no lo que se pidio.
 */
public interface RecoleccionesSvc {

    List<EntregaListDto> listarRecolecciones();

    EstadisticasEntregasDto obtenerEstadisticas();

    RecoleccionDetalleDto obtenerRecoleccion(Long idRecoleccion);

    /** Agenda la recoleccion (pedidos_cana.fecha_recogido) y ajusta la estimacion de viajes. */
    RecoleccionDetalleDto programar(Long idRecoleccion, ProgramarRecoleccionDto datos);

    /**
     * Registra un viaje de vuelta. Si es el primero, abre la recoleccion y
     * mueve el pedido a "Recolectado / en ruta a bodega".
     */
    RecoleccionDetalleDto registrarViaje(RegistrarViajeDto viaje);

    /**
     * Cierra la recoleccion, mueve el pedido a "Finalizado" y sella
     * pedidos_cana.fecha_recogido. No exige que haya vuelto todo: lo que quede
     * pendiente es el faltante del pedido.
     */
    RecoleccionDetalleDto marcarFinalizada(Long idRecoleccion);
}
