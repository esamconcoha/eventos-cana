package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.CalendarioItemDto;
import com.canabackend.cana.services.CalendarioSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/calendario")
public class CalendarioController {
    @Autowired
    private CalendarioSvc service;

    /**
     * @param desde primer dia visible de la grilla (inclusive)
     * @param hasta ultimo dia visible de la grilla (inclusive)
     */
    @GetMapping("/privado/agenda")
    public ResponseEntity<List<CalendarioItemDto>> agenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(this.service.obtenerAgenda(desde, hasta));
    }
}
