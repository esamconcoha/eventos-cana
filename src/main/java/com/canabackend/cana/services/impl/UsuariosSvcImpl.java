package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.CrearUsuarioDto;
import com.canabackend.cana.dtos.GetUsuarioDto;
import com.canabackend.cana.dtos.ModificarUsuarioDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.Direcciones;
import com.canabackend.cana.models.Usuarios;
import com.canabackend.cana.projections.GetUsuarioProjection;
import com.canabackend.cana.repositories.CatalogosCanaRepository;
import com.canabackend.cana.repositories.DireccionesRepository;
import com.canabackend.cana.repositories.UsuariosRepository;
import com.canabackend.cana.services.DireccionesSvc;
import com.canabackend.cana.services.UsuariosSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuariosSvcImpl implements UsuariosSvc {
    @Autowired
    private UsuariosRepository repository;
    @Autowired
    private CatalogosCanaRepository catalogosCanaRepository;
    @Autowired
    private DireccionesSvc direccionesSvc;
    @Autowired
    private DireccionesRepository direccionesRepository;
    @Override
    public Optional<Usuarios> findByCorreo(String correo){
        return repository.findByCorreo(correo);
    }

    @Override
    public List<Usuarios> findAllByCorreo(String correo){
        return repository.findAllByCorreo(correo);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public Usuarios save(CrearUsuarioDto usuario){
        Optional<Usuarios> usuarioExistente = repository.findByDpiNitUsuario(usuario.getDpiNitUsuario());
        Optional<Usuarios> usuarioExistenteCorreo = repository.findByCorreo(usuario.getCorreo());
        if (usuarioExistente.isPresent() || usuarioExistenteCorreo.isPresent() ) {
            throw new MSCanaException(ErrorEnum.I_USUARIO_EXISTENTE);
        }
        Usuarios usuarioNuevo = new Usuarios();
        usuarioNuevo.setDpiNitUsuario(usuario.getDpiNitUsuario());
        usuarioNuevo.setNombresUsuario(usuario.getNombresUsuario());
        usuarioNuevo.setApellidosUsuario(usuario.getApellidosUsuario());
        usuarioNuevo.setTelefonoUsuario(usuario.getTelefonoUsuario());
        usuarioNuevo.setCorreo(usuario.getCorreo());
        usuarioNuevo.setEsRepresentante(usuario.getEsRepresentante());
        usuarioNuevo.setEstadoUsuario(Boolean.TRUE);
        usuarioNuevo.setRol(usuario.getRol());
        usuarioNuevo.setEsEmpresa(usuario.getEsEmpresa());
        usuarioNuevo.setContrasenia(usuario.getContrasenia());
        usuarioNuevo.setUsuarioCreo(usuario.getUsuarioCreo());

        repository.save(usuarioNuevo);
        List<Direcciones> direcciones = usuario.getDirecciones();
        direcciones.forEach(d -> d.setNitDpi(usuarioNuevo.getDpiNitUsuario()));
        this.direccionesRepository.saveAll(direcciones);
        return usuarioNuevo;
    }

    @Override
    public List<GetUsuarioDto> getUsuariosInternosActivos(){
        List<GetUsuarioProjection> usuarios = repository.getUsuariosInternosActivos();
        List<GetUsuarioDto> usuariosDto = new ArrayList<>();
        for (GetUsuarioProjection usuario : usuarios) {
            String rolUsuario= this.catalogosCanaRepository.getNombreCatalogo(usuario.getRol().longValue());
            GetUsuarioDto itemUsuario= new GetUsuarioDto();
            itemUsuario.setDpiNitUsuario(usuario.getDpiNitUsuario());
            itemUsuario.setNombresUsuario(usuario.getNombresUsuario());
            itemUsuario.setApellidosUsuario(usuario.getApellidosUsuario());
            itemUsuario.setTelefonoUsuario(usuario.getTelefonoUsuario());
            itemUsuario.setCorreoUsuario(usuario.getCorreoUsuario());
            itemUsuario.setEstadoUsuario(usuario.getEstadoUsuario());
            itemUsuario.setRol(rolUsuario);
            itemUsuario.setIdRol(usuario.getRol());
            itemUsuario.setDirecciones(this.direccionesSvc.getDireccionesUsuario(usuario.getDpiNitUsuario()));
            usuariosDto.add(itemUsuario);
        }
        return usuariosDto;
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void inactivarUsuario(String nitDPi){
        Optional<Usuarios> usuarioInactivar= this.repository.findByDpiNitUsuario(nitDPi);
        usuarioInactivar.get().setEstadoUsuario(Boolean.FALSE);
        this.repository.save(usuarioInactivar.get());

    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void modificarUsuario(ModificarUsuarioDto usuarioModificar){
        Optional<Usuarios> usuarioModificarDb= this.repository.findByDpiNitUsuario(usuarioModificar.getDpiNit());
        usuarioModificarDb.get().setNombresUsuario(usuarioModificar.getNombresUsuario());
        usuarioModificarDb.get().setApellidosUsuario(usuarioModificar.getApellidosUsuario());
        usuarioModificarDb.get().setTelefonoUsuario(usuarioModificar.getTelefonoUsuario());
        usuarioModificarDb.get().setCorreo(usuarioModificar.getCorreo());
        usuarioModificarDb.get().setRol(usuarioModificar.getRol());
        this.repository.save(usuarioModificarDb.get());
        for (int i = 0; i < usuarioModificar.getDirecciones().size(); i++) {
            Optional<Direcciones> direccion= this.direccionesRepository.findById(usuarioModificar.getDirecciones().get(i).getIdDireccion());
            direccion.get().setDireccion(usuarioModificar.getDirecciones().get(i).getDireccion());
            this.direccionesRepository.save(direccion.get());
        }
    }

}
