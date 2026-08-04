package com.canabackend.cana.services;

public interface ReporteCotizacionSvc {

    /**
     * Genera el documento PDF de una cotizacion ya guardada.
     *
     * @param idCotizacion Identificador de la cotizacion
     * @return Contenido del PDF como arreglo de bytes (blob)
     */
    byte[] generarPdfCotizacion(Long idCotizacion);
}
