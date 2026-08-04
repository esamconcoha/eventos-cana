package com.canabackend.cana.exceptions;

import io.micrometer.common.util.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class Response {
    private static final String ERROR = "error";
    private static final String EXITO = "success";
    private final Integer status;
    private final String type;
    private final Integer code;
    private final String message;
    private final List detail;

    private Response(int pEstadoHttp, List pDetalle) {
        this.status = pEstadoHttp;
        this.type = EXITO;
        this.code = null;
        this.message = null;
        this.detail = pDetalle;
    }

    private Response(ErrorEnum pError, List pDetalle, Object[] pParamError) {
        this.status = pError.getEstadoHttp();
        this.type = ERROR;
        this.code = pError.getCodigo();
        this.message = MessageFormat.format(pError.getDescripcion(), pParamError);
        this.detail = pDetalle;
    }

    private Response(ErrorEnum pError, List pDetalle, Object[] pParamError, String messageOverwrite) {
        this.status = pError.getEstadoHttp();
        this.type = ERROR;
        this.code = pError.getCodigo();
        this.message = MessageFormat.format(messageOverwrite, pParamError);
        this.detail = pDetalle;
    }

    public static Response ok(Object pDto) {
        return new Response(EstadoHttp.OK, Arrays.asList(pDto));
    }

    public static Response ok(List pDetalle) {
        return new Response(EstadoHttp.OK, pDetalle);
    }

    public static Response created(Object pDto) {
        return new Response(EstadoHttp.CREATED, Arrays.asList(pDto));
    }

    public static Response error(ErrorEnum pError) {
        return new Response(pError, null, null);
    }

    public static Response error(ErrorEnum pError, List pDetalle) {
        return new Response(pError, pDetalle, null);
    }

    public static Response error(ErrorEnum pError, Object[] pParamError) {
        return new Response(pError, null, pParamError);
    }

    public static Response error(ErrorEnum pError, List pDetalle, String messageOverwrite){
        return StringUtils.isBlank(messageOverwrite)
                ? error(pError, pDetalle)
                : new Response(pError, pDetalle, null, messageOverwrite);
    }

    public static Response error(ErrorEnum pError, Object[] pParamError, String messageOverwrite) {
        return StringUtils.isBlank(messageOverwrite)
                ? error(pError, pParamError)
                : new Response(pError, null, pParamError, messageOverwrite);
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getType() {
        return this.type;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public Integer getSizeDetail() {
        if(this.detail != null) {
            return this.detail.size();
        } else {
            return null;
        }
    }

    public List getDetail() {
        return this.detail;
    }
}
