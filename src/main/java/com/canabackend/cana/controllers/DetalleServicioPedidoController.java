package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.PedidoDto;
import com.canabackend.cana.services.DetalleServicioPedidoSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/detalleServicioPedido")
public class DetalleServicioPedidoController {
    @Autowired
    private DetalleServicioPedidoSvc service;

    /** @param realizado false permite revertir si se marco por error */
    @PutMapping("/privado/marcarRealizado/{idDetalleServPedido}")
    public ResponseEntity<PedidoDto> marcarRealizado(
            @PathVariable Long idDetalleServPedido,
            @RequestParam(defaultValue = "true") boolean realizado) {
        return ResponseEntity.ok(this.service.marcarRealizado(idDetalleServPedido, realizado));
    }
}
