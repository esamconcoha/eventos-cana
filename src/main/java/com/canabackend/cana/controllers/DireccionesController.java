package com.canabackend.cana.controllers;

import com.canabackend.cana.models.Direcciones;
import com.canabackend.cana.services.DireccionesSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/direcciones")
public class DireccionesController {
    @Autowired
    private DireccionesSvc service;
    @GetMapping("getDireccionesByDpiNit/{dpiNit}")
    public ResponseEntity<List<Direcciones>>  getDireccionesByDpiNit(@PathVariable String dpiNit){
        return ResponseEntity.ok(this.service.getDireccionesUsuario(dpiNit));
    }
}
