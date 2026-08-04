package com.canabackend.cana.controllers;

import com.canabackend.cana.models.CategoriasServicio;
import com.canabackend.cana.services.CategoriasServiciosSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/servicios")
public class CategoriasServiciosController {

    @Autowired
    private CategoriasServiciosSvc service;

    @GetMapping("/privado/listarCategorias")
    public ResponseEntity<List<CategoriasServicio>> listarCategorias() {
        return ResponseEntity.ok(service.listarCategorias());
    }
}
