package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modificacion de una cotizacion todavia no confirmada.
 *
 * No incluye dpiNitUsuarioCotizacion ni cotizacionConfirmada: el primero
 * identifica a quien la creo y no cambia; el segundo lo mueve el flujo de
 * confirmacion, no una edicion.
 *
 * detalleCotizacion y detalleServicioCotizacion REEMPLAZAN por completo a los
 * existentes (se borran y se recrean), igual que en actualizarPedido: no es un
 * merge linea por linea.
 */
@Data
public class ActualizarCotizacionDto {
    String nombreClienteCotizacion;
    String direccionClienteCotizacion;
    Long telefonoClienteCotizacion;
    LocalDateTime fechaHoraEvento;
    String codTipoEvento;
    List<DetalleCotizacionDto> detalleCotizacion;
    List<DetalleServicioCotizacionDto> detalleServicioCotizacion;
}
