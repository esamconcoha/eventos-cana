package com.canabackend.cana.services;

import com.canabackend.cana.models.DocumentosCotizacion;

public interface DocumentosCotizacionSvc {

    /**
     * Persiste el PDF generado de una cotizacion para poder consultarlo despues.
     */
    DocumentosCotizacion guardarDocumentoCotizacion(Long idCotizacion, byte[] contenido, String usuarioGenero);

    /**
     * Recupera el ultimo documento vigente de una cotizacion.
     */
    DocumentosCotizacion obtenerUltimoDocumento(Long idCotizacion);
}
