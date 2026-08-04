package com.canabackend.cana.dtos;

import lombok.Data;

@Data
public class GetCatalogoByNombreDto {
    Integer idCatalogo;
    String codigo;
    String nombre;

}
