package com.canabackend.cana.services.impl;

import com.canabackend.cana.models.CategoriasServicio;
import com.canabackend.cana.repositories.CategoriasServiciosRepository;
import com.canabackend.cana.services.CategoriasServiciosSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriasServiciosSvcImpl implements CategoriasServiciosSvc {

    @Autowired
    private CategoriasServiciosRepository repository;

    @Override
    public List<CategoriasServicio> listarCategorias() {
        return repository.listarCategoriasActivas();
    }
}
