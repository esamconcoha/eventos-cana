package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.GetCatalogoByNombreDto;
import com.canabackend.cana.projections.GetCatalogoByNombreProjection;
import com.canabackend.cana.repositories.CatalogosCanaRepository;
import com.canabackend.cana.services.CatalogosCanaSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogosCanaSvcImpl implements CatalogosCanaSvc {
    @Autowired
    private CatalogosCanaRepository repository;
    @Override
    public List<GetCatalogoByNombreDto> getCatalogoByNombre(String nombreCatalogo){
        List<GetCatalogoByNombreProjection> catalogoDB=repository.getCatalogoByNombre(nombreCatalogo);
        List<GetCatalogoByNombreDto> catalogoDTO=new ArrayList<>();
        for(GetCatalogoByNombreProjection GetCatalogoByNombreProjection:catalogoDB){
            GetCatalogoByNombreDto catalogoItem=new GetCatalogoByNombreDto();
            catalogoItem.setIdCatalogo(GetCatalogoByNombreProjection.getIdCatalogo());
            catalogoItem.setCodigo(GetCatalogoByNombreProjection.getCodigo());
            catalogoItem.setNombre(GetCatalogoByNombreProjection.getNombre());
            catalogoDTO.add(catalogoItem);
        }
        return catalogoDTO;
    }

}
