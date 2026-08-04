package com.canabackend.cana.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ViajeEntregaDto {
    Long idViaje;
    LocalDateTime fechaInicioViaje;
    LocalDateTime fechaFinViaje;
    List<ViajeItemDto> items;
}
