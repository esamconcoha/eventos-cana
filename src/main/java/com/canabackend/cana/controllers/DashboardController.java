package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.DashboardResumenDto;
import com.canabackend.cana.services.DashboardSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired
    private DashboardSvc service;

    @GetMapping("/privado/resumen")
    public ResponseEntity<DashboardResumenDto> resumen() {
        return ResponseEntity.ok(this.service.obtenerResumen());
    }
}
