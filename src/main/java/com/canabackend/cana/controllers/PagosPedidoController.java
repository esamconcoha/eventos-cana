package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.EstadoCuentaPedidoDto;
import com.canabackend.cana.dtos.PagoPedidoListDto;
import com.canabackend.cana.dtos.RegistrarPagoDto;
import com.canabackend.cana.services.PagosPedidoSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagosPedido")
public class PagosPedidoController {
    @Autowired
    private PagosPedidoSvc service;

    @PostMapping("/privado/registrarPago")
    public ResponseEntity<Void> registrarPago(@RequestBody RegistrarPagoDto pago) {
        this.service.registrarPago(pago);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/privado/anularPago/{idPago}")
    public ResponseEntity<Void> anularPago(@PathVariable Long idPago) {
        this.service.anularPago(idPago);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/privado/listar/{correlativoPedido}")
    public ResponseEntity<List<PagoPedidoListDto>> listarPagos(@PathVariable String correlativoPedido) {
        return ResponseEntity.ok(this.service.listarPagos(correlativoPedido));
    }

    @GetMapping("/privado/estadoCuenta/{correlativoPedido}")
    public ResponseEntity<EstadoCuentaPedidoDto> obtenerEstadoCuenta(@PathVariable String correlativoPedido) {
        return ResponseEntity.ok(this.service.obtenerEstadoCuenta(correlativoPedido));
    }
}
