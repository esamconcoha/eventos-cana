package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CotizacionListDto {
    Long idCotizacion;
    String codigoCotizacion;
    String nombreClienteCotizacion;
    String direccionClienteCotizacion;
    Long telefonoClienteCotizacion;
    LocalDate fechaCotizacion;
    LocalDateTime fechaHoraEvento;
    String estadoCotizacion;
    String dpiNitUsuarioCotizacion;
    /** Necesario para poder precargar el formulario al modificarla. */
    String codTipoEvento;
    /**
     * true si el pedido generado por esta cotizacion (ya confirmada) fue
     * cancelado. Alimenta la nota "Pedido cancelado" en el listado.
     */
    Boolean pedidoCancelado;
    List<DetalleItemCotizacionListDto> detalles;
    List<DetalleServicioCotizacionListDto> detallesServicios;
}
