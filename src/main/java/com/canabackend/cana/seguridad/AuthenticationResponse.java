package com.canabackend.cana.seguridad;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {

    private String jwt;
    private String nombre;
    private String dpi;

    /** catalogos_cana.id_catalogo del rol. */
    private Integer rol;

    /**
     * catalogos_cana.codigo del rol. Es el valor sobre el que debe ramificar el
     * frontend para mostrar u ocultar opciones: el id se corre si se resiembra
     * el catalogo y el nombre cambia con cualquier ajuste de redaccion.
     */
    private String codigoRol;

    /** catalogos_cana.nombre del rol, solo para mostrar. */
    private String nombreRol;
}
