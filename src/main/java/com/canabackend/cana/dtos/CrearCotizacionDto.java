package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CrearCotizacionDto {
    String nombreClienteCotizacion;
    String direccionClienteCotizacion;
    Long telefonoClienteCotizacion;
    Boolean cotizacionConfirmada;
    String dpiNitUsuarioCotizacion;
    /** Opcional: si no viene, al crear se usa hoy y al editar se conserva. */
    LocalDate fechaCotizacion;
    LocalDateTime fechaHoraEvento;
    String codTipoEvento;
    List<DetalleCotizacionDto> detalleCotizacion;
    List<DetalleServicioCotizacionDto> detalleServicioCotizacion;
}
