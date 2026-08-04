package com.canabackend.cana.dtos;

import com.canabackend.cana.models.Direcciones;
import lombok.Data;

import java.util.List;
@Data
public class ModificarUsuarioDto {
    String dpiNit;
    String nombresUsuario;
    String apellidosUsuario;
    Long telefonoUsuario;
    String correo;
    Long rol;
    List<Direcciones>  direcciones;
}
