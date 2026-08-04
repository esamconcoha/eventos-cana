package com.canabackend.cana.services;

import com.canabackend.cana.dtos.GetCatalogoByNombreDto;

import java.util.List;

public interface CatalogosCanaSvc {

    public List<GetCatalogoByNombreDto> getCatalogoByNombre(String nombreCatalogo);
}
