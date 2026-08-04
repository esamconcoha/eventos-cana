package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegistrarViajeDto {
    Long idEntrega;
    LocalDateTime fechaInicioViaje;
    /** Opcional: un viaje puede registrarse abierto y cerrarse despues. */
    LocalDateTime fechaFinViaje;
    List<ViajeItemRequestDto> items;
}
