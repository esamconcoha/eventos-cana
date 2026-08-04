package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.ActualizarPedidoDto;
import com.canabackend.cana.dtos.GuardarPedidoDto;
import com.canabackend.cana.dtos.HistorialEstadoPedidoDto;
import com.canabackend.cana.dtos.PedidoDto;
import com.canabackend.cana.services.PedidosCanaSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidosCanaController {
    @Autowired
    private PedidosCanaSvc service;

    @GetMapping("/privado/listarPedidos")
    public ResponseEntity<List<PedidoDto>> listarPedidos() {
        return ResponseEntity.ok(this.service.listarPedidos());
    }

    @GetMapping("/privado/obtenerPedido/{correlativoPedido}")
    public ResponseEntity<PedidoDto> obtenerPedido(@PathVariable String correlativoPedido) {
        return ResponseEntity.ok(this.service.obtenerPedido(correlativoPedido));
    }

    @PostMapping("/privado/guardarPedido")
    public ResponseEntity<PedidoDto> guardarPedido(@RequestBody GuardarPedidoDto pedido) {
        return ResponseEntity.ok(this.service.guardarPedido(pedido));
    }

    @PutMapping("/privado/actualizarPedido/{correlativoPedido}")
    public ResponseEntity<PedidoDto> actualizarPedido(@PathVariable String correlativoPedido,
                                                       @RequestBody ActualizarPedidoDto pedido) {
        return ResponseEntity.ok(this.service.actualizarPedido(correlativoPedido, pedido));
    }

    @PutMapping("/privado/cambiarEstado/{correlativoPedido}/{idEstado}")
    public ResponseEntity<PedidoDto> cambiarEstado(@PathVariable String correlativoPedido, @PathVariable Long idEstado) {
        return ResponseEntity.ok(this.service.cambiarEstado(correlativoPedido, idEstado));
    }

    @PutMapping("/privado/cancelarPedido/{correlativoPedido}")
    public ResponseEntity<PedidoDto> cancelarPedido(@PathVariable String correlativoPedido) {
        return ResponseEntity.ok(this.service.cancelarPedido(correlativoPedido));
    }

    @GetMapping("/privado/historialEstados/{correlativoPedido}")
    public ResponseEntity<List<HistorialEstadoPedidoDto>> historialEstados(@PathVariable String correlativoPedido) {
        return ResponseEntity.ok(this.service.historialEstados(correlativoPedido));
    }
}
