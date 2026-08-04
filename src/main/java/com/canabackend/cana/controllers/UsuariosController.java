package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.GetUsuarioDto;
import com.canabackend.cana.dtos.ModificarUsuarioDto;
import com.canabackend.cana.services.UsuariosSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {
    @Autowired
    private UsuariosSvc service;

    @GetMapping("/privado/listarUsuarios")
    ResponseEntity<List<GetUsuarioDto>> getUsuariosPrivado(){
        return ResponseEntity.ok(this.service.getUsuariosInternosActivos());
    }

    @PutMapping("/privado/inactivarUsuario/{nitDpi}")
    public ResponseEntity<Void>inactivarUsuario(@PathVariable String nitDpi){
        this.service.inactivarUsuario(nitDpi);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/privado/modificarUsuario")
    public  ResponseEntity<Void>modificarUsuario(@RequestBody ModificarUsuarioDto usuarioModificar){
        this.service.modificarUsuario(usuarioModificar);
        return ResponseEntity.ok().build();
    }
}
