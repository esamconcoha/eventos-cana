package com.canabackend.cana.services.impl;

import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.DocumentosEntrega;
import com.canabackend.cana.repositories.DocumentosEntregaRepository;
import com.canabackend.cana.repositories.EntregasPedidoRepository;
import com.canabackend.cana.services.DocumentosEntregaSvc;
import com.canabackend.cana.utils.ConstanciaFirmadaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class DocumentosEntregaSvcImpl implements DocumentosEntregaSvc {

    @Autowired
    private DocumentosEntregaRepository repository;
    @Autowired
    private EntregasPedidoRepository entregasPedidoRepository;

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public DocumentosEntrega subirConstanciaFirmada(Long idEntrega, MultipartFile archivo, String usuarioSubio) {
        if (!this.entregasPedidoRepository.existsById(idEntrega)) {
            throw new MSCanaException(ErrorEnum.ENTREGA_NOT_FOUND);
        }
        validarArchivo(archivo);

        // Solo se conserva una constancia firmada vigente por entrega: si ya
        // habia una (p.ej. se volvio a subir por un error de foto), se
        // desactiva en vez de borrarla, para no perder el historial.
        this.repository.findFirstByIdEntregaAndEstadoRegistroTrueOrderByFechaGeneracionDesc(idEntrega)
                .ifPresent(anterior -> {
                    anterior.setEstadoRegistro(false);
                    this.repository.save(anterior);
                });

        try {
            DocumentosEntrega documento = new DocumentosEntrega();
            documento.setIdEntrega(idEntrega);
            documento.setNombreDocumento(nombreOriginal(archivo));
            documento.setTipoDocumento(ConstanciaFirmadaConstants.TIPO_DOCUMENTO_CONSTANCIA_FIRMADA);
            documento.setContentType(archivo.getContentType());
            documento.setContenido(archivo.getBytes());
            documento.setFechaGeneracion(LocalDateTime.now());
            documento.setUsuarioGenero(usuarioSubio);
            documento.setEstadoRegistro(Boolean.TRUE);
            return this.repository.save(documento);
        } catch (IOException e) {
            throw new MSCanaException(ErrorEnum.I_ERROR_SUBIR_CONSTANCIA_FIRMADA, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentosEntrega obtenerConstanciaFirmada(Long idEntrega) {
        return this.repository
                .findFirstByIdEntregaAndEstadoRegistroTrueOrderByFechaGeneracionDesc(idEntrega)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.CONSTANCIA_FIRMADA_NOT_FOUND));
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new MSCanaException(ErrorEnum.ARCHIVO_VACIO);
        }
        if (!ConstanciaFirmadaConstants.CONTENT_TYPES_PERMITIDOS.contains(archivo.getContentType())) {
            throw new MSCanaException(ErrorEnum.TIPO_ARCHIVO_NO_PERMITIDO);
        }
        if (archivo.getSize() > ConstanciaFirmadaConstants.TAMANIO_MAXIMO_BYTES) {
            throw new MSCanaException(ErrorEnum.ARCHIVO_EXCEDE_TAMANO_MAXIMO);
        }
    }

    private String nombreOriginal(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();
        return (nombre != null && !nombre.isBlank()) ? nombre : "constancia-firmada";
    }
}
