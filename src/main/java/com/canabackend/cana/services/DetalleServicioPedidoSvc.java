package com.canabackend.cana.services;

import com.canabackend.cana.dtos.PedidoDto;

public interface DetalleServicioPedidoSvc {

    /**
     * Confirma que un servicio del pedido (montaje, decoracion...) ya se
     * realizo, o revierte esa confirmacion.
     *
     * Reemplaza al estado "Montando / Decorando": el montaje es paralelo a los
     * viajes de entrega y opcional segun lo que el cliente haya contratado, y
     * una maquina de estados no admite ninguna de las dos cosas.
     *
     * @param realizado true sella la fecha actual; false la limpia
     * @return el pedido completo, para que la pantalla se refresque sin otra llamada
     */
    PedidoDto marcarRealizado(Long idDetalleServPedido, boolean realizado);
}
