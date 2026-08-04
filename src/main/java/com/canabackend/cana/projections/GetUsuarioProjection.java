package com.canabackend.cana.projections;

public interface GetUsuarioProjection {
    String getDpiNitUsuario();
    String getNombresUsuario();
    String getApellidosUsuario();
    Integer getTelefonoUsuario();
    String getCorreoUsuario();
    Boolean getEstadoUsuario();
    Integer getRol();
}
