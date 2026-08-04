package com.canabackend.cana.services;

import com.canabackend.cana.models.DocumentosEntrega;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentosEntregaSvc {

    /**
     * Persiste la constancia de entrega FIRMADA (foto o escaneo) subida por el
     * usuario. Si ya habia una vigente para esta entrega, la desactiva: solo
     * se conserva una version vigente a la vez, la anterior queda en el
     * historial con estado_registro=false.
     */
    DocumentosEntrega subirConstanciaFirmada(Long idEntrega, MultipartFile archivo, String usuarioSubio);

    /** Recupera la constancia firmada vigente de una entrega. */
    DocumentosEntrega obtenerConstanciaFirmada(Long idEntrega);
}
