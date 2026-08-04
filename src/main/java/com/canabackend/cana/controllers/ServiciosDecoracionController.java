package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.GetServiciosDecoracionDto;
import com.canabackend.cana.models.ServiciosDecoracion;
import com.canabackend.cana.services.ServiciosDecoracionSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicios")
public class ServiciosDecoracionController {

    @Autowired
    private ServiciosDecoracionSvc service;

    @PostMapping("/privado/guardarServicio")
    public ResponseEntity<?> guardarServicio(@RequestBody ServiciosDecoracion serviciosDecoracion){
        ServiciosDecoracion servicioGuardado = service.createServiciosDecoracion(serviciosDecoracion);
        return ResponseEntity.ok(servicioGuardado);
    }

    @PutMapping("/privado/inactivarServicio/{id}")
    public ResponseEntity<?> inactivarServicio(@PathVariable Long id) {
        service.eliminarServiciosDecoracion(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/privado/listarServicios")
    public ResponseEntity<List<GetServiciosDecoracionDto>> listarServicios() {
        return ResponseEntity.ok(service.listarServicios());
    }

    @PutMapping("/privado/editarServicio/{id}")
    public ResponseEntity<Void> editarServicio(@RequestBody ServiciosDecoracion serviciosDecoracion, @PathVariable Long id) {
        service.modificarServicio(serviciosDecoracion, id);
        return ResponseEntity.noContent().build();
    }
}
