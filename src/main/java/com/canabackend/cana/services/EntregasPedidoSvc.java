package com.canabackend.cana.services;

import com.canabackend.cana.dtos.CrearEntregaDto;
import com.canabackend.cana.dtos.EntregaDetalleDto;
import com.canabackend.cana.dtos.EntregaListDto;
import com.canabackend.cana.dtos.EstadisticasEntregasDto;
import com.canabackend.cana.dtos.PedidoDisponibleDto;
import com.canabackend.cana.dtos.RegistrarViajeDto;

import java.util.List;

public interface EntregasPedidoSvc {

    List<EntregaListDto> listarEntregas();

    EstadisticasEntregasDto obtenerEstadisticas();

    /** Detalle con los viajes ya expandidos y el avance de despacho por item. */
    EntregaDetalleDto obtenerEntrega(Long idEntrega);

    /** Pedidos activos, en estado previo a la entrega y sin entrega creada. */
    List<PedidoDisponibleDto> pedidosDisponibles();

    EntregaDetalleDto crearEntrega(CrearEntregaDto entrega);

    /**
     * Registra un viaje con sus items, incrementa el contador de viajes reales
     * y, si es el primero, abre la entrega (fecha_inicio_entrega) y mueve el
     * pedido al estado "En ruta entrega".
     */
    EntregaDetalleDto registrarViaje(RegistrarViajeDto viaje);

    /** Cierra la entrega y mueve el pedido al estado "Entregado". */
    EntregaDetalleDto marcarFinalizada(Long idEntrega);
}
