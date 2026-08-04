package com.canabackend.cana.services;

import com.canabackend.cana.models.MantenimientoSalones;

import java.util.List;

public interface MantenimientoSalonesSvc {

    List<MantenimientoSalones> listarSalones();

    MantenimientoSalones crearSalon(MantenimientoSalones salon);

    void editarSalon(MantenimientoSalones salon, Long idSalon);

    void desactivarSalon(Long idSalon);
}
