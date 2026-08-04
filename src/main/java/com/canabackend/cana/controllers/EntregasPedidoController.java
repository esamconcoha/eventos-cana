package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.CrearEntregaDto;
import com.canabackend.cana.dtos.EntregaDetalleDto;
import com.canabackend.cana.dtos.EntregaListDto;
import com.canabackend.cana.dtos.EstadisticasEntregasDto;
import com.canabackend.cana.dtos.PedidoDisponibleDto;
import com.canabackend.cana.dtos.RegistrarViajeDto;
import com.canabackend.cana.models.DocumentosEntrega;
import com.canabackend.cana.services.DocumentosEntregaSvc;
import com.canabackend.cana.services.EntregasPedidoSvc;
import com.canabackend.cana.services.ReporteEntregaSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/entregas")
public class EntregasPedidoController {
    @Autowired
    private EntregasPedidoSvc service;
    @Autowired
    private ReporteEntregaSvc reporteEntregaSvc;
    @Autowired
    private DocumentosEntregaSvc documentosEntregaSvc;

    @GetMapping("/privado/listarEntregas")
    public ResponseEntity<List<EntregaListDto>> listarEntregas() {
        return ResponseEntity.ok(this.service.listarEntregas());
    }

    @GetMapping("/privado/estadisticas")
    public ResponseEntity<EstadisticasEntregasDto> estadisticas() {
        return ResponseEntity.ok(this.service.obtenerEstadisticas());
    }

    @GetMapping("/privado/obtenerEntrega/{idEntrega}")
    public ResponseEntity<EntregaDetalleDto> obtenerEntrega(@PathVariable Long idEntrega) {
        return ResponseEntity.ok(this.service.obtenerEntrega(idEntrega));
    }

    @GetMapping("/privado/pedidosDisponibles")
    public ResponseEntity<List<PedidoDisponibleDto>> pedidosDisponibles() {
        return ResponseEntity.ok(this.service.pedidosDisponibles());
    }

    @PostMapping("/privado/crearEntrega")
    public ResponseEntity<EntregaDetalleDto> crearEntrega(@RequestBody CrearEntregaDto entrega) {
        return ResponseEntity.ok(this.service.crearEntrega(entrega));
    }

    @PostMapping("/privado/registrarViaje")
    public ResponseEntity<EntregaDetalleDto> registrarViaje(@RequestBody RegistrarViajeDto viaje) {
        return ResponseEntity.ok(this.service.registrarViaje(viaje));
    }

    @PutMapping("/privado/marcarFinalizada/{idEntrega}")
    public ResponseEntity<EntregaDetalleDto> marcarFinalizada(@PathVariable Long idEntrega) {
        return ResponseEntity.ok(this.service.marcarFinalizada(idEntrega));
    }

    /** Constancia de entrega en PDF (lo despachado + servicios) con area de firma. */
    @GetMapping("/privado/constancia/{idEntrega}")
    public ResponseEntity<byte[]> constancia(@PathVariable Long idEntrega) {
        byte[] pdf = this.reporteEntregaSvc.generarPdfEntrega(idEntrega);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"constancia-entrega.pdf\"")
                .body(pdf);
    }

    /**
     * Sube la constancia YA FIRMADA por el cliente (foto o escaneo de la
     * constancia de arriba). Devuelve la entrega actualizada para que el
     * frontend refresque el flag sin pedirla aparte.
     */
    @PostMapping("/privado/constanciaFirmada/{idEntrega}")
    public ResponseEntity<EntregaDetalleDto> subirConstanciaFirmada(@PathVariable Long idEntrega,
                                                                     @RequestParam("archivo") MultipartFile archivo,
                                                                     @RequestParam("usuarioSubio") String usuarioSubio) {
        this.documentosEntregaSvc.subirConstanciaFirmada(idEntrega, archivo, usuarioSubio);
        return ResponseEntity.ok(this.service.obtenerEntrega(idEntrega));
    }

    /** Descarga la constancia firmada vigente (PDF o imagen, segun se subio). */
    @GetMapping("/privado/constanciaFirmada/{idEntrega}")
    public ResponseEntity<byte[]> obtenerConstanciaFirmada(@PathVariable Long idEntrega) {
        DocumentosEntrega documento = this.documentosEntregaSvc.obtenerConstanciaFirmada(idEntrega);
        MediaType tipo = MediaType.parseMediaType(
                documento.getContentType() != null ? documento.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + documento.getNombreDocumento() + "\"")
                .body(documento.getContenido());
    }
}
