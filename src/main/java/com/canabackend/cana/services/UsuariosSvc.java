package com.canabackend.cana.services;

import com.canabackend.cana.dtos.CrearUsuarioDto;
import com.canabackend.cana.dtos.GetUsuarioDto;
import com.canabackend.cana.dtos.ModificarUsuarioDto;
import com.canabackend.cana.models.Usuarios;

import java.util.List;
import java.util.Optional;

public interface UsuariosSvc {
    public Optional<Usuarios> findByCorreo(String correo);
    /** Todos los usuarios registrados con ese correo (puede haber duplicados por borrado logico). */
    public List<Usuarios> findAllByCorreo(String correo);
    public Usuarios save(CrearUsuarioDto usuario);
    public List<GetUsuarioDto> getUsuariosInternosActivos();
    public void inactivarUsuario(String nitDPi);
    public void modificarUsuario(ModificarUsuarioDto usuarioModificar);
}
