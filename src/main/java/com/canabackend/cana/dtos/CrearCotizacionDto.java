package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CrearCotizacionDto {
    String nombreClienteCotizacion;
    String direccionClienteCotizacion;
    Long telefonoClienteCotizacion;
    Boolean cotizacionConfirmada;
    String dpiNitUsuarioCotizacion;
    LocalDateTime fechaHoraEvento;
    String codTipoEvento;
    List<DetalleCotizacionDto> detalleCotizacion;
    List<DetalleServicioCotizacionDto> detalleServicioCotizacion;
}
