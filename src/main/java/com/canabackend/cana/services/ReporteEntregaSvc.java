package com.canabackend.cana.services;

public interface ReporteEntregaSvc {

    /**
     * Genera la constancia de entrega en PDF: lo despachado (articulos con
     * cantidad enviada) y los servicios contratados, con un area de firma para
     * que el cliente acuse recibo.
     */
    byte[] generarPdfEntrega(Long idEntrega);
}
