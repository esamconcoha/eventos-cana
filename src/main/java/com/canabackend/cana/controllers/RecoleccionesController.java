package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.EntregaListDto;
import com.canabackend.cana.dtos.EstadisticasEntregasDto;
import com.canabackend.cana.dtos.ProgramarRecoleccionDto;
import com.canabackend.cana.dtos.RecoleccionDetalleDto;
import com.canabackend.cana.dtos.RegistrarViajeDto;
import com.canabackend.cana.services.RecoleccionesSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recolecciones")
public class RecoleccionesController {
    @Autowired
    private RecoleccionesSvc service;

    @GetMapping("/privado/listarRecolecciones")
    public ResponseEntity<List<EntregaListDto>> listarRecolecciones() {
        return ResponseEntity.ok(this.service.listarRecolecciones());
    }

    @GetMapping("/privado/estadisticas")
    public ResponseEntity<EstadisticasEntregasDto> estadisticas() {
        return ResponseEntity.ok(this.service.obtenerEstadisticas());
    }

    @GetMapping("/privado/obtenerRecoleccion/{idRecoleccion}")
    public ResponseEntity<RecoleccionDetalleDto> obtenerRecoleccion(@PathVariable Long idRecoleccion) {
        return ResponseEntity.ok(this.service.obtenerRecoleccion(idRecoleccion));
    }

    @PutMapping("/privado/programar/{idRecoleccion}")
    public ResponseEntity<RecoleccionDetalleDto> programar(@PathVariable Long idRecoleccion,
                                                           @RequestBody(required = false) ProgramarRecoleccionDto datos) {
        return ResponseEntity.ok(this.service.programar(idRecoleccion, datos));
    }

    @PostMapping("/privado/registrarViaje")
    public ResponseEntity<RecoleccionDetalleDto> registrarViaje(@RequestBody RegistrarViajeDto viaje) {
        return ResponseEntity.ok(this.service.registrarViaje(viaje));
    }

    @PutMapping("/privado/marcarFinalizada/{idRecoleccion}")
    public ResponseEntity<RecoleccionDetalleDto> marcarFinalizada(@PathVariable Long idRecoleccion) {
        return ResponseEntity.ok(this.service.marcarFinalizada(idRecoleccion));
    }
}
