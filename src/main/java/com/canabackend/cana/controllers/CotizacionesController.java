package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.ActualizarCotizacionDto;
import com.canabackend.cana.dtos.ConfirmarCotizacionDto;
import com.canabackend.cana.dtos.CotizacionListDto;
import com.canabackend.cana.dtos.CrearCotizacionDto;
import com.canabackend.cana.dtos.HistorialEstadoCotizacionDto;
import com.canabackend.cana.services.CotizacionesSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cotizaciones")
public class CotizacionesController {
    @Autowired
    private CotizacionesSvc service;

    @GetMapping("/privado/listarCotizaciones")
    public ResponseEntity<List<CotizacionListDto>> listarCotizaciones() {
        return ResponseEntity.ok(this.service.listarCotizaciones());
    }

    @GetMapping("/privado/historialEstados/{idCotizacion}")
    public ResponseEntity<List<HistorialEstadoCotizacionDto>> historialEstados(@PathVariable Long idCotizacion) {
        return ResponseEntity.ok(this.service.historialEstados(idCotizacion));
    }

    @PostMapping("/privado/guardarCotizacion")
    public ResponseEntity<byte[]> guardarCotizacion(@RequestBody CrearCotizacionDto cotizacion) {
        byte[] pdf = this.service.guardarCotizacion(cotizacion);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cotizacion.pdf\"")
                .body(pdf);
    }

    /** Devuelve el PDF regenerado, igual que guardarCotizacion. */
    @PutMapping("/privado/actualizarCotizacion/{idCotizacion}")
    public ResponseEntity<byte[]> actualizarCotizacion(@PathVariable Long idCotizacion,
                                                       @RequestBody ActualizarCotizacionDto cotizacion) {
        byte[] pdf = this.service.actualizarCotizacion(idCotizacion, cotizacion);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cotizacion.pdf\"")
                .body(pdf);
    }

    @GetMapping("/privado/documento/{idCotizacion}")
    public ResponseEntity<byte[]> obtenerDocumento(@PathVariable Long idCotizacion) {
        byte[] pdf = this.service.obtenerDocumentoCotizacion(idCotizacion);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cotizacion.pdf\"")
                .body(pdf);
    }

    @PutMapping("/privado/confirmarCotizacion/{id}")
    public ResponseEntity<Void> confirmarCotizacion(@PathVariable Long id,
                                                    @RequestBody(required = false) ConfirmarCotizacionDto confirmacion) {
        this.service.confirmarCotizacion(id, confirmacion);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/privado/cancelarCotizacion/{id}")
    public ResponseEntity<Void> cancelarCotizacion(@PathVariable Long id) {
        this.service.cancelarCotizacion(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/privado/eliminarCotizacion/{id}")
    public ResponseEntity<Void> eliminarCotizacion(@PathVariable Long id) {
        this.service.eliminarCotizacion(id);
        return ResponseEntity.ok().build();
    }
}
