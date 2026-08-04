package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadoActualDto {
    Long idEstado;
    String codigoEstado;
    String nombreEstado;
}
