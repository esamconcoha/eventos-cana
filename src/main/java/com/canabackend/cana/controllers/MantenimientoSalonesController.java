package com.canabackend.cana.controllers;

import com.canabackend.cana.models.MantenimientoSalones;
import com.canabackend.cana.services.MantenimientoSalonesSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salones")
public class MantenimientoSalonesController {
    @Autowired
    private MantenimientoSalonesSvc service;

    @GetMapping("/privado/listarSalones")
    public ResponseEntity<List<MantenimientoSalones>> listarSalones() {
        return ResponseEntity.ok(this.service.listarSalones());
    }

    @PostMapping("/privado/guardarSalon")
    public ResponseEntity<MantenimientoSalones> guardarSalon(@RequestBody MantenimientoSalones salon) {
        return ResponseEntity.ok(this.service.crearSalon(salon));
    }

    @PutMapping("/privado/editarSalon/{id}")
    public ResponseEntity<Void> editarSalon(@RequestBody MantenimientoSalones salon, @PathVariable Long id) {
        this.service.editarSalon(salon, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/privado/eliminarSalon/{id}")
    public ResponseEntity<Void> eliminarSalon(@PathVariable Long id) {
        this.service.desactivarSalon(id);
        return ResponseEntity.noContent().build();
    }
}
