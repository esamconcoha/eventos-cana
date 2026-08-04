package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoProximoDto {
    String correlativoPedido;
    LocalDateTime fechaEvento;
    String nombreCliente;
    String nombreTipoEvento;
    String ubicacion;
    String codigoEstadoPedido;
    String nombreEstadoPedido;
}
