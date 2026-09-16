package com.canabackend.cana.utils;

import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;

/**
 * Descuentos a nivel de linea (articulo o servicio) de cotizaciones y pedidos.
 *
 * <p>El descuento vive en la linea y no en la cabecera porque el negocio lo
 * aplica por articulo/servicio ("el mantel va de cortesia", "20% en el
 * mobiliario"), y porque asi la trazabilidad sobrevive el paso cotizacion ->
 * pedido: al confirmar, cada linea se copia con su descuento tal cual se pacto.
 *
 * <p>El calculo del monto tiene su gemelo en la BD
 * ({@code cana.fn_descuento_linea}), que es el que usan los totales del estado
 * de cuenta, la reporteria y el PDF. El de aca sirve para validar antes de
 * grabar; las dos formulas tienen que recortar igual.
 */
public final class DescuentoConstants {

    private DescuentoConstants() {
    }

    /** Sin descuento. Es el default de toda linea, incluidas las historicas. */
    public static final String TIPO_NINGUNO = "NIN";
    /** Porcentaje sobre el subtotal de la linea; valor entre 0 y 100. */
    public static final String TIPO_PORCENTAJE = "POR";
    /** Monto fijo en Q sobre el subtotal de la linea. */
    public static final String TIPO_MONTO = "MON";
    /** Exoneracion total: la linea queda en Q 0.00 y el valor se ignora. */
    public static final String TIPO_EXONERACION = "EXO";

    /** Largo de motivo_descuento en la BD; los motivos mas largos se recortan. */
    public static final int MAX_LARGO_MOTIVO = 200;

    public static boolean esTipoValido(String tipo) {
        return TIPO_NINGUNO.equals(tipo) || TIPO_PORCENTAJE.equals(tipo)
                || TIPO_MONTO.equals(tipo) || TIPO_EXONERACION.equals(tipo);
    }

    /**
     * Un tipo ausente o vacio es "sin descuento": el frontend viejo (y cualquier
     * integracion que no conozca la funcionalidad) sigue funcionando sin mandar
     * el campo. Un tipo presente pero desconocido si es un error.
     */
    public static String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return TIPO_NINGUNO;
        }
        String normalizado = tipo.trim().toUpperCase();
        if (!esTipoValido(normalizado)) {
            throw new MSCanaException(ErrorEnum.DESCUENTO_TIPO_INVALIDO);
        }
        return normalizado;
    }

    /**
     * Valor que acompania al tipo ya normalizado. NIN y EXO no llevan valor (se
     * fuerza a 0 aunque venga algo), POR y MON exigen uno positivo.
     */
    public static double normalizarValor(String tipoNormalizado, Double valor) {
        if (TIPO_NINGUNO.equals(tipoNormalizado) || TIPO_EXONERACION.equals(tipoNormalizado)) {
            return 0d;
        }
        if (valor == null || valor <= 0) {
            throw new MSCanaException(ErrorEnum.DESCUENTO_VALOR_INVALIDO);
        }
        if (TIPO_PORCENTAJE.equals(tipoNormalizado) && valor > 100) {
            throw new MSCanaException(ErrorEnum.DESCUENTO_VALOR_INVALIDO);
        }
        return valor;
    }

    /** Sin tipo no hay motivo que guardar: evita motivos huerfanos en la BD. */
    public static String normalizarMotivo(String tipoNormalizado, String motivo) {
        if (TIPO_NINGUNO.equals(tipoNormalizado) || motivo == null || motivo.isBlank()) {
            return null;
        }
        String limpio = motivo.trim();
        return limpio.length() > MAX_LARGO_MOTIVO ? limpio.substring(0, MAX_LARGO_MOTIVO) : limpio;
    }

    /**
     * Gemelo de {@code cana.fn_descuento_linea}: nunca devuelve mas que el
     * bruto ni menos que cero, asi un porcentaje o monto excesivo recorta la
     * linea a Q 0.00 en vez de generar un neto negativo.
     */
    public static double montoDescuento(double bruto, String tipo, double valor) {
        if (bruto <= 0) {
            return 0d;
        }
        if (TIPO_EXONERACION.equals(tipo)) {
            return bruto;
        }
        if (TIPO_PORCENTAJE.equals(tipo)) {
            double porcentaje = Math.min(Math.max(valor, 0d), 100d);
            return Math.round(bruto * porcentaje / 100d * 100d) / 100d;
        }
        if (TIPO_MONTO.equals(tipo)) {
            return Math.min(Math.max(valor, 0d), bruto);
        }
        return 0d;
    }

    public static double netoLinea(double bruto, String tipo, double valor) {
        return bruto - montoDescuento(bruto, tipo, valor);
    }
}
