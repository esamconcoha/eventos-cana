package com.canabackend.cana.services;

import com.canabackend.cana.models.Direcciones;

import java.util.List;

public interface DireccionesSvc {
    public List<Direcciones> getDireccionesUsuario(String dpiNitUsuario);
}
