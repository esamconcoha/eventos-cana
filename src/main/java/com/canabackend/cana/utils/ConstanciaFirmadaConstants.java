package com.canabackend.cana.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constancia de entrega FIRMADA: la sube el usuario (foto o escaneo) despues
 * de que el cliente firma la constancia impresa por {@link ReporteEntregaSvc
 * generarPdfEntrega}. No confundir con esa constancia generada: esta es el
 * archivo real con la firma, y se guarda en cana.documentos_entrega.
 */
public final class ConstanciaFirmadaConstants {

    private ConstanciaFirmadaConstants() {
    }

    public static final String TIPO_DOCUMENTO_CONSTANCIA_FIRMADA = "CONSTANCIA_FIRMADA";

    /**
     * En campo la firma casi siempre se sube como foto de celular, no como
     * escaneo: por eso se acepta imagen ademas de PDF.
     */
    public static final List<String> CONTENT_TYPES_PERMITIDOS = Collections.unmodifiableList(
            Arrays.asList("application/pdf", "image/jpeg", "image/png", "image/webp"));

    /**
     * Una foto de celular moderna facilmente pasa de 5MB; 10MB da margen sin
     * abrir la puerta a archivos absurdos.
     */
    public static final long TAMANIO_MAXIMO_BYTES = 10L * 1024 * 1024;
}
