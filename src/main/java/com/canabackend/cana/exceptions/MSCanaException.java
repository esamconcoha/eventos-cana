package com.canabackend.cana.exceptions;

import java.util.List;

public class MSCanaException extends RuntimeException {
    private final ErrorEnum error;
    private final transient List<ErrorDetail> errores;
    private final transient Object[] paramError;
    private final String messageOverwrite;

    public MSCanaException(ErrorEnum pError) {
        super(pError.toString());
        this.error = pError;
        this.errores = null;
        this.paramError = null;
        this.messageOverwrite = null;
    }

    public MSCanaException(ErrorEnum pError, Object[] pParamError) {
        super(pError.toString());
        this.error = pError;
        this.errores = null;
        this.paramError = pParamError;
        this.messageOverwrite = null;
    }

    public MSCanaException(ErrorEnum pError, List<ErrorDetail> pErrores) {
        super(pError.toString());
        this.error = pError;
        this.errores = pErrores;
        this.paramError = null;
        this.messageOverwrite = null;
    }

    /**
     * El super() estaba comentado, con lo que este constructor recibia la
     * excepcion original y la tiraba a la basura: sin causa encadenada y sin
     * mensaje, el stack trace de lo que fallo de verdad no existia en ningun
     * lado. Es la razon por la que un error al generar un PDF llegaba al
     * frontend como "codigo 3004" y en los logs del servidor no aparecia nada.
     */
    public MSCanaException(ErrorEnum pError, Throwable pCause) {
        super(pError.toString(), pCause);
        this.error = pError;
        this.errores = null;
        this.paramError = null;
        this.messageOverwrite = null;
    }

    public MSCanaException(String mensaje) {
        super(mensaje);
        this.error = null;
        this.errores = null;
        this.paramError = null;
        this.messageOverwrite = null;
    }

    public MSCanaException(ErrorEnum pError, String message) {
        super(pError.toString());
        this.error = pError;
        this.errores = null;
        this.paramError = null;
        this.messageOverwrite = message;
    }

    public ErrorEnum getError() {
        return error;
    }

    public List<ErrorDetail> getErrores() {
        return errores;
    }

    public Object[] getParamError() {
        return paramError;
    }

    public String getMessageOverwrite() {
        return messageOverwrite;
    }
}
