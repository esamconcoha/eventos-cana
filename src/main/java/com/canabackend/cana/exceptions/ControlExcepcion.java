package com.canabackend.cana.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ControlExcepcion {

    private static final Logger logger = LoggerFactory.getLogger(ControlExcepcion.class);

    @ExceptionHandler(value = {MSCanaException.class})
    @ResponseBody
    public ResponseEntity MsException(MSCanaException pException) {

        ErrorEnum error = pException.getError();
        int estadoHttp = error.getEstadoHttp();
        String message = pException.getMessageOverwrite();

        // Este handler atrapa TODAS las MSCanaException, asi que era el unico
        // lugar del que podia salir un log y no escribia ninguno: un 500 salia
        // al frontend sin dejar rastro en el servidor.
        // Los 4xx son reglas de negocio esperadas (usuario repetido, rango de
        // fechas invalido): se anotan en una linea, sin stack trace, para no
        // llenar los logs de Fly. Los 5xx son fallas de verdad y van con la
        // excepcion completa, que es lo unico que permite diagnosticarlas.
        if (estadoHttp >= 500) {
            logger.error("Error {} ({}): {}", error.getCodigo(), error.name(),
                    error.getDescripcion(), pException);
        } else {
            logger.warn("Error {} ({}): {}", error.getCodigo(), error.name(), error.getDescripcion());
        }

        if(CollectionUtils.isEmpty(pException.getErrores())) {
            return ResponseEntity
                    .status(estadoHttp)
                    .body(Response.error(error, pException.getParamError(), message));
        } else {
            return ResponseEntity
                    .status(estadoHttp)
                    .body(Response.error(error, pException.getErrores(), message));
        }
    }

    /**
     * Spring rechaza un archivo que supera max-file-size ANTES de que llegue
     * al controller (no pasa por MSCanaException), asi que sin este handler el
     * frontend recibiria un 500 generico en vez del codigo 4038 que ya sabe
     * mostrar como "el archivo no puede superar los 10MB".
     */
    @ExceptionHandler(value = {MaxUploadSizeExceededException.class})
    @ResponseBody
    public ResponseEntity archivoExcedeTamanoMaximo() {
        ErrorEnum error = ErrorEnum.ARCHIVO_EXCEDE_TAMANO_MAXIMO;
        return ResponseEntity.status(error.getEstadoHttp()).body(Response.error(error));
    }
}
