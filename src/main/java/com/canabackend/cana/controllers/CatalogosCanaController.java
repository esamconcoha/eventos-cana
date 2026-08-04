package com.canabackend.cana.controllers;

import com.canabackend.cana.services.CatalogosCanaSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalogosCana")
public class CatalogosCanaController {
    @Autowired
    private CatalogosCanaSvc service;

    @GetMapping("getCatalogoByNombre/{nombre}")
    public ResponseEntity<?> getCatalogoByNombre(@PathVariable("nombre") String nombre){
        return ResponseEntity.ok(service.getCatalogoByNombre(nombre));
    }

}
