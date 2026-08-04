package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HistorialEstadoPedidoDto {
    Long idEstado;
    String codigoEstado;
    String nombreEstado;
    LocalDateTime fechaHoraInicio;
    LocalDateTime fechaHoraFin;
}
