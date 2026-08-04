package com.canabackend.cana.services;

import com.canabackend.cana.dtos.EstadoCuentaPedidoDto;
import com.canabackend.cana.dtos.PagoPedidoListDto;
import com.canabackend.cana.dtos.RegistrarPagoDto;

import java.util.List;

public interface PagosPedidoSvc {

    void registrarPago(RegistrarPagoDto pago);

    void anularPago(Long idPago);

    List<PagoPedidoListDto> listarPagos(String correlativoPedido);

    EstadoCuentaPedidoDto obtenerEstadoCuenta(String correlativoPedido);

    /**
     * Recalcula estado_pago/pagado a partir del monto total vigente del pedido
     * (montoTotalPedido tras editar detalles) contra los pagos activos existentes,
     * sin modificar los pagos ya registrados.
     */
    void recalcularEstadoPago(String correlativoPedido);
}
