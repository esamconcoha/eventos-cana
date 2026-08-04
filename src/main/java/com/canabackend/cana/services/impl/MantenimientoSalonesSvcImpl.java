package com.canabackend.cana.services.impl;

import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.MantenimientoSalones;
import com.canabackend.cana.repositories.MantenimientoSalonesRepository;
import com.canabackend.cana.services.MantenimientoSalonesSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MantenimientoSalonesSvcImpl implements MantenimientoSalonesSvc {

    @Autowired
    private MantenimientoSalonesRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<MantenimientoSalones> listarSalones() {
        return this.repository.findAll();
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public MantenimientoSalones crearSalon(MantenimientoSalones salon) {
        salon.setIdSalon(null);
        salon.setEstadoSalon(true);
        return this.repository.save(salon);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void editarSalon(MantenimientoSalones salon, Long idSalon) {
        MantenimientoSalones existente = this.repository.findById(idSalon)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.SALON_NOT_FOUND));
        existente.setNombreSalon(salon.getNombreSalon());
        existente.setDireccionSalon(salon.getDireccionSalon());
        this.repository.save(existente);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void desactivarSalon(Long idSalon) {
        MantenimientoSalones existente = this.repository.findById(idSalon)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.SALON_NOT_FOUND));
        existente.setEstadoSalon(false);
        this.repository.save(existente);
    }
}
