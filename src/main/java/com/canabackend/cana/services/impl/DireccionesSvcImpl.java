package com.canabackend.cana.services.impl;

import com.canabackend.cana.models.Direcciones;
import com.canabackend.cana.repositories.DireccionesRepository;
import com.canabackend.cana.services.DireccionesSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DireccionesSvcImpl implements DireccionesSvc {

    @Autowired
    private DireccionesRepository direccionesRepository;

    @Override
    public List<Direcciones> getDireccionesUsuario(String dpiNitUsuario){
        List<Direcciones> direccionesUsuario=this.direccionesRepository.findDireccionesBynitDpi(dpiNitUsuario);
        return direccionesUsuario;
    }

}
