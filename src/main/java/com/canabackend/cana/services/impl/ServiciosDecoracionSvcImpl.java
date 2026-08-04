package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.GetServiciosDecoracionDto;
import com.canabackend.cana.models.ServiciosDecoracion;
import com.canabackend.cana.projections.GetServiciosDecoracionProjection;
import com.canabackend.cana.repositories.ServiciosDecoracionRepository;
import com.canabackend.cana.services.ServiciosDecoracionSvc;
import com.canabackend.cana.validators.ServiciosDecoracionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiciosDecoracionSvcImpl implements ServiciosDecoracionSvc {
    @Autowired
    private ServiciosDecoracionRepository repository;
    @Autowired
    private ServiciosDecoracionValidator validator;

    @Override
    public ServiciosDecoracion createServiciosDecoracion(ServiciosDecoracion serviciosDecoracion) {
    this.validator.validarNombre(serviciosDecoracion.getNombreServicio());
    serviciosDecoracion.setEstadoServicio(true);
    return repository.save(serviciosDecoracion);
    }

    @Override
    public void eliminarServiciosDecoracion(Long idServiciosDecoracion){
        ServiciosDecoracion serviciosDecoracion = repository.findById(idServiciosDecoracion).get();
        serviciosDecoracion.setEstadoServicio(false);
        repository.save(serviciosDecoracion);
    }

    @Override
    public List<GetServiciosDecoracionDto> listarServicios() {
        List<GetServiciosDecoracionProjection> serviciosDB = repository.listarServicios();
        List<GetServiciosDecoracionDto> serviciosDTO = new ArrayList<>();
        for (GetServiciosDecoracionProjection servicio : serviciosDB) {
            GetServiciosDecoracionDto servicioDto = new GetServiciosDecoracionDto();
            servicioDto.setIdServicio(servicio.getIdServicio());
            servicioDto.setIdCategoria(servicio.getIdCategoria());
            servicioDto.setNombreCategoria(servicio.getNombreCategoria());
            servicioDto.setNombreServicio(servicio.getNombreServicio());
            servicioDto.setDescripcionServicio(servicio.getDescripcionServicio());
            servicioDto.setUnidadMedida(servicio.getUnidadMedida());
            servicioDto.setRequiereDetalle(servicio.getRequiereDetalle());
            servicioDto.setEstadoServicio(servicio.getEstadoServicio());
            serviciosDTO.add(servicioDto);
        }
        return serviciosDTO;
    }

    @Override
    public void modificarServicio(ServiciosDecoracion serviciosDecoracion, Long idServicio) {
        ServiciosDecoracion servicioExistente = this.repository.findById(idServicio).get();
        this.validator.validarNombreEdicion(serviciosDecoracion.getNombreServicio(), idServicio);
        servicioExistente.setIdCategoria(serviciosDecoracion.getIdCategoria());
        servicioExistente.setNombreServicio(serviciosDecoracion.getNombreServicio());
        servicioExistente.setDescripcionServicio(serviciosDecoracion.getDescripcionServicio());
        servicioExistente.setUnidadMedida(serviciosDecoracion.getUnidadMedida());
        servicioExistente.setRequiereDetalle(serviciosDecoracion.getRequiereDetalle());
        this.repository.save(servicioExistente);
    }
}
