package com.canabackend.cana.services;

import com.canabackend.cana.dtos.GetServiciosDecoracionDto;
import com.canabackend.cana.models.ServiciosDecoracion;

import java.util.List;

public interface ServiciosDecoracionSvc {

    public ServiciosDecoracion createServiciosDecoracion(ServiciosDecoracion serviciosDecoracion);
    public void eliminarServiciosDecoracion(Long idServiciosDecoracion);
    public List<GetServiciosDecoracionDto> listarServicios();
    public void modificarServicio(ServiciosDecoracion serviciosDecoracion, Long idServicio);
}
