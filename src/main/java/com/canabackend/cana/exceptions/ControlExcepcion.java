package com.canabackend.cana.exceptions;

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
    @ExceptionHandler(value = {MSCanaException.class})
    @ResponseBody
    public ResponseEntity MsException(MSCanaException pException) {

        ErrorEnum error = pException.getError();
        int estadoHttp = error.getEstadoHttp();
        String message = pException.getMessageOverwrite();

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
