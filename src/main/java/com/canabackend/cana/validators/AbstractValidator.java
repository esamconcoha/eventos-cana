package com.canabackend.cana.validators;

import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import io.micrometer.common.util.StringUtils;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

public abstract class AbstractValidator {

    /**
     * <p>
     * Metodo que valida que el valor no sea nulo o espacios en blanco.</p>
     *
     * @param valor Valor a verificar
     * @param error Codigo de error que se debe lanzar
     * @throws MSCanaException Si el valor es nulo o cadena en blanco
     */
    protected void blanks(String valor, ErrorEnum error) throws MSCanaException {
        if (StringUtils.isBlank(valor)) {
            throw new MSCanaException(error);
        }
    }

    protected void DictamenDistinto(ErrorEnum error) throws MSCanaException {
        throw new MSCanaException(error);
    }

    /**
     * <p>
     * Metodo que valida si un valor es nulo.</p>
     *
     * @param valor Valor a verificar
     * @param error Codigo de error que se debe lanzar
     * @throws MSCanaException Si el valor es nulo
     */
    protected void nulls(Object valor, ErrorEnum error) throws MSCanaException {
        if (valor == null) {
            throw new MSCanaException(error);
        }
    }

    protected void nullsBolean(boolean valor, ErrorEnum error) throws MSCanaException {
        if (valor == false) {
            throw new MSCanaException(error);
        }
    }

    protected void nullsBoleanTrue(boolean valor, ErrorEnum error) throws MSCanaException {
        if (valor == true) {
            throw new MSCanaException(error);
        }
    }

    /**
     * <p>
     * Metodo que valida si un valor es nulo.</p>
     *
     * @param valor Valor a verificar
     * @param error Codigo de error que se debe lanzar
     * @throws MSCanaException Si el valor es nulo
     */
    protected void collectionNulls(Collection valor, ErrorEnum error) throws MSCanaException {
        if (CollectionUtils.isEmpty(valor)) {
            throw new MSCanaException(error);
        }
    }

    /**
     * <p>
     * Metodo que valida si un valor cumple con cierta expresion regular.</p>
     *
     * @param valor Valor a verificar
     * @param regex Expresion regular que debe cumplir el valor
     * @param error Codigo de error que se debe de lanzar
     * @throws MSCanaException Si el valor no cumple con la expresion regular
     */
    protected void matches(String valor, String regex, ErrorEnum error) throws MSCanaException {
        if (StringUtils.isNotBlank(valor) && !valor.matches(regex)) {
            throw new MSCanaException(error);
        }
    }


    /**
     * <p>
     * Metodo que valida que la primer fecha sea menor a la segunda fecha
     * tomando en cuenta el tiempo.</p>
     *
     * @param fecha1 Fecha a verificar
     * @param fecha2 Fecha que no se debe sobrepasar
     * @param error Codigo de error que se debe de lanzar
     * @throws MSCanaException Si la fecha 1 es mayor a la fecha 2 tomando en
     * cuenta el tiempo
     */
    protected void futureTime(Date fecha1, Date fecha2, ErrorEnum error)
            throws MSCanaException {
        if (fecha1.getTime() > fecha2.getTime()) {
            throw new MSCanaException(error);
        }
    }

    /**
     * <p>
     * Metodo que valida si la cadena cumple con un formato de fecha
     * especifico.</p>
     *
     * @param fecha Cadena a verificar
     * @param pattern Formato de fecha que debe cumplir la cadena
     * @param error Codigo de error que se debe de lanzar
     * @return Date Objeto creado a partir de la cadena o nulo si la cadena es
     * vacia o nulo
     * @throws MSCanaException Si la cadena no cumple con el formato
     */
    protected Date matchesDate(String fecha, String pattern, ErrorEnum error)
            throws MSCanaException {
        if (StringUtils.isNotBlank(fecha)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false);
                return sdf.parse(fecha);
            } catch (ParseException e) {
                throw new MSCanaException(error, e);
            }
        } else {
            return null;
        }
    }

    /**
     * *
     *
     * @param fecha Cadena a verificar
     * @param pattern Formato de fecha que debe cumplir la cadena
     * @param error Codigo de error que se debe de lanzar
     * @return Date Objeto creado a partir de la cadena o nulo si la cadena es
     * vacia o nulo
     * @throws MSCanaException Si la cadena no cumple con el
     */
    protected static LocalDateTime matchesDateTime(String fecha, String pattern, ErrorEnum error)
            throws MSCanaException {
        if (fecha != null && !fecha.trim().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(fecha, formatter);
            } catch (DateTimeParseException e) {
                throw new MSCanaException(error, e);
            }
        } else {
            return null;
        }
    }

    /**
     * <p>
     * Metodo que valida si una coleccion contiene un determinado item.</p>
     *
     * @param collection Coleccion a verificar
     * @param valor Valor a buscar
     * @param error Codigo de error que se debe lanzar
     * @throws MSCanaException Si el valor no esta en la coleccion.
     */
    protected void contains(Collection collection, Object valor, ErrorEnum error) throws MSCanaException {
        if (!collection.contains(valor)) {
            throw new MSCanaException(error);
        }
    }

    /**
     * <p>
     * Metodo que valida si existe un elemento.</p>
     *
     * @param valor Valor a verificar
     * @param error Codigo de error que se debe lanzar
     * @throws MSCanaException Si el valor existe.
     */
    protected void exists(Optional valor, ErrorEnum error) throws MSCanaException {
        if (valor.isPresent()) {
            throw new MSCanaException(error);
        }
    }

    /**
     * <p>
     * Metodo que valida si un valor es diferente a null.</p>
     *
     * @param valor Valor a verificar
     * @param error Codigo de error que se debe lanzar
     * @throws MSCanaException Si el valor es diferente a null
     */
    protected void notNull(Object valor, ErrorEnum error) throws MSCanaException {
        if (valor != null) {
            throw new MSCanaException(error);
        }
    }

    protected void formatoDeFechaInfo(ErrorEnum error) throws MSCanaException {
        throw new MSCanaException(error);
    }

    protected void mensajeDeError(ErrorEnum error) throws MSCanaException {
        throw new MSCanaException(error);
    }
}
