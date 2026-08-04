package com.canabackend.cana.services;

import com.canabackend.cana.dtos.ActualizarPedidoDto;
import com.canabackend.cana.dtos.GuardarPedidoDto;
import com.canabackend.cana.dtos.HistorialEstadoPedidoDto;
import com.canabackend.cana.dtos.PedidoDto;

import java.time.LocalDate;
import java.util.List;

public interface PedidosCanaSvc {

    List<PedidoDto> listarPedidos();

    PedidoDto obtenerPedido(String correlativoPedido);

    PedidoDto guardarPedido(GuardarPedidoDto pedido);

    PedidoDto actualizarPedido(String correlativoPedido, ActualizarPedidoDto pedido);

    PedidoDto cambiarEstado(String correlativoPedido, Long idEstado);

    /**
     * Cancela el pedido: lo mueve al estado terminal "Evento cancelado" (ECA).
     * Es la unica via para dar de baja un evento ya confirmado; en Cotizaciones
     * se refleja como la nota "Pedido cancelado" sobre la cotizacion de origen.
     */
    PedidoDto cancelarPedido(String correlativoPedido);

    List<HistorialEstadoPedidoDto> historialEstados(String correlativoPedido);

    /**
     * Crea el pedido homologo de una cotizacion recien confirmada (mismos
     * detalles/detallesServicios) junto con su entrega ya abierta. No hace nada
     * si la cotizacion ya tiene un pedido asociado.
     *
     * @param fechaEntrega               fecha pactada, va a pedidos_cana.fecha_entrega
     * @param cantidadViajesAproximados  estimacion inicial de viajes, minimo 1
     */
    void crearPedidoDesdeCotizacion(Long idCotizacion, LocalDate fechaEntrega, Integer cantidadViajesAproximados);
}
