package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdResponseUserDto {
    String nombre;
    String dpi;
    Integer rol;
    /** catalogos_cana.codigo: estable, es sobre esto que se deciden permisos. */
    String codigoRol;
    /** catalogos_cana.nombre: solo para mostrar. */
    String nombreRol;

}
