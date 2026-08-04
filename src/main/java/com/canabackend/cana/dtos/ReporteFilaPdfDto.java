package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila de las tablas del PDF. Las cinco secciones (articulos, servicios,
 * clientes, cartera, faltantes) comparten esta forma y se separan por
 * {@code seccion}, que es el grupo del reporte Jasper.
 *
 * <p>Los encabezados viajan repetidos en cada fila porque cada seccion titula
 * sus columnas distinto y el group header de Jasper lee los campos de la
 * primera fila del grupo. Los valores llegan ya formateados: dar formato en el
 * jrxml obligaria a un patron por columna y aca hay columnas de moneda,
 * unidades y porcentaje en la misma posicion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteFilaPdfDto {
    private String seccion;
    private String encabezado1;
    private String encabezado2;
    private String encabezado3;
    private String encabezado4;
    private String encabezado5;
    private String titulo;
    private String subtitulo;
    private String valor1;
    private String valor2;
    private String valor3;
}
