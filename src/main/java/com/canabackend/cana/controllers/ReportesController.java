package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.ReporteGeneralDto;
import com.canabackend.cana.services.ReporteEstadisticoSvc;
import com.canabackend.cana.services.ReportesSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
public class ReportesController {

    @Autowired
    private ReportesSvc service;
    @Autowired
    private ReporteEstadisticoSvc reporteEstadisticoSvc;

    /** Tablero completo. Sin fechas devuelve los ultimos 12 meses. */
    @GetMapping("/privado/general")
    public ResponseEntity<ReporteGeneralDto> general(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(this.service.obtenerReporteGeneral(desde, hasta));
    }

    /** El mismo tablero en PDF, con las graficas incluidas. */
    @GetMapping("/privado/general/pdf")
    public ResponseEntity<byte[]> generalPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] pdf = this.reporteEstadisticoSvc.generarPdf(desde, hasta);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte-estadistico.pdf\"")
                .body(pdf);
    }
}
