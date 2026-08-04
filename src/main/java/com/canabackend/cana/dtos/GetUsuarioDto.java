package com.canabackend.cana.dtos;

import com.canabackend.cana.models.Direcciones;
import lombok.Data;

import java.util.List;

@Data
public class GetUsuarioDto {
  String dpiNitUsuario;
  String nombresUsuario;
  String apellidosUsuario;
  Integer telefonoUsuario;
  String correoUsuario;
  Boolean estadoUsuario;
  String rol;
  Integer idRol;
  List<Direcciones> direcciones;
}
