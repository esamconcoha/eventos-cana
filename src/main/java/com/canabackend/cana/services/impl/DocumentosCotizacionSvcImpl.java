package com.canabackend.cana.services.impl;

import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.DocumentosCotizacion;
import com.canabackend.cana.repositories.DocumentosCotizacionRepository;
import com.canabackend.cana.services.DocumentosCotizacionSvc;
import com.canabackend.cana.utils.CotizacionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DocumentosCotizacionSvcImpl implements DocumentosCotizacionSvc {

    @Autowired
    private DocumentosCotizacionRepository repository;

    @Override
    public DocumentosCotizacion guardarDocumentoCotizacion(Long idCotizacion, byte[] contenido, String usuarioGenero) {
        DocumentosCotizacion documento = new DocumentosCotizacion();
        documento.setIdCotizacion(idCotizacion);
        documento.setNombreDocumento(String.format("Cotizacion_COT-%06d.pdf", idCotizacion));
        documento.setTipoDocumento(CotizacionConstants.TIPO_DOCUMENTO_COTIZACION);
        documento.setContentType(CotizacionConstants.CONTENT_TYPE_PDF);
        documento.setContenido(contenido);
        documento.setFechaGeneracion(LocalDateTime.now());
        documento.setUsuarioGenero(usuarioGenero);
        documento.setEstadoRegistro(Boolean.TRUE);
        return this.repository.save(documento);
    }

    @Override
    public DocumentosCotizacion obtenerUltimoDocumento(Long idCotizacion) {
        return this.repository
                .findFirstByIdCotizacionAndEstadoRegistroTrueOrderByFechaGeneracionDesc(idCotizacion)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.DOCUMENTO_NOT_FOUND));
    }
}
