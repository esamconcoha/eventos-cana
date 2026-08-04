package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.PedidoDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.DetalleServicioPedido;
import com.canabackend.cana.repositories.DetalleServicioPedidoRepository;
import com.canabackend.cana.services.DetalleServicioPedidoSvc;
import com.canabackend.cana.services.PedidosCanaSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DetalleServicioPedidoSvcImpl implements DetalleServicioPedidoSvc {

    @Autowired
    private DetalleServicioPedidoRepository detalleServicioPedidoRepository;
    @Autowired
    private PedidosCanaSvc pedidosCanaSvc;

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public PedidoDto marcarRealizado(Long idDetalleServPedido, boolean realizado) {
        DetalleServicioPedido detalle = this.detalleServicioPedidoRepository.findById(idDetalleServPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.SERVICIO_PEDIDO_NOT_FOUND));

        // Se guarda el momento y no un booleano: responde lo mismo (null =
        // pendiente) y ademas deja registrado el cuando.
        detalle.setFechaRealizado(realizado ? LocalDateTime.now() : null);
        this.detalleServicioPedidoRepository.save(detalle);

        return this.pedidosCanaSvc.obtenerPedido(detalle.getCorrelativoPedido());
    }
}
