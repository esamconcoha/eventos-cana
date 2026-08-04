package com.canabackend.cana.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ConfirmarCotizacionDto {

    /**
     * Fecha de entrega pactada; se copia tal cual a pedidos_cana.fecha_entrega
     * del pedido que nace de esta cotizacion.
     *
     * El nombre canonico es fechaEntrega, igual que en GuardarPedidoDto y que
     * en la columna: es el mismo dato y no conviene que tenga dos nombres.
     * Se acepta fechaEntregaEstimada como alias para no romper el frontend ya
     * construido; ese alias se puede quitar cuando el front migre.
     */
    @JsonAlias("fechaEntregaEstimada")
    LocalDate fechaEntrega;

    Integer cantidadViajesAproximados;
}
