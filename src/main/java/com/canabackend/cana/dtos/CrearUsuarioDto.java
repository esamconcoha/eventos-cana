package com.canabackend.cana.dtos;

import com.canabackend.cana.models.Direcciones;
import lombok.Data;

import java.util.List;
@Data
public class CrearUsuarioDto {
    String dpiNitUsuario;
    String nombresUsuario;
    String apellidosUsuario;
    Long telefonoUsuario;
    String correo;
    Boolean esRepresentante;
    Boolean estadoUsuario;
    Long rol;
    Boolean esEmpresa;
    String contrasenia;
    String usuarioCreo;
    List<Direcciones> direcciones;
}
