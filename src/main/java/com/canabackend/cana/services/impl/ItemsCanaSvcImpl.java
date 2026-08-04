package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.GetItemsExistentesDto;
import com.canabackend.cana.models.ItemsCana;
import com.canabackend.cana.projections.views.GetItemsExistentesProjectionView;
import com.canabackend.cana.repositories.ItemsCanaRepository;
import com.canabackend.cana.services.ItemsCanaSvc;
import com.canabackend.cana.validators.ItemsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemsCanaSvcImpl implements ItemsCanaSvc {
    @Autowired
    private ItemsCanaRepository repository;
    @Autowired
    private ItemsValidator validator;

    @Override
    public List<GetItemsExistentesDto> getItemsExistentes(){
    List<GetItemsExistentesProjectionView> itemsDb=this.repository.getItemsExistentes();
    List<GetItemsExistentesDto> itemsDto=new ArrayList<>();
    for(GetItemsExistentesProjectionView item:itemsDb){
        GetItemsExistentesDto itemDto=new GetItemsExistentesDto();
        itemDto.setIdItem(item.getIdItem());
        itemDto.setIdTipoItem(item.getIdTipoItem());
        itemDto.setCostoItem(item.getCostoItem());
        itemDto.setDescripcionItem(item.getDescripcionItem());
        itemDto.setCantidadItem(item.getCantidadItem());
        itemDto.setObservaciones(item.getObservaciones());
        itemDto.setCantidadFaltante(item.getCantidadFaltante());
        itemDto.setNombreItem(item.getNombreItem());
        itemDto.setEstadoItem(item.getEstadoItem());
        itemsDto.add(itemDto);
    }
    return itemsDto;
    }

    @Override
    public ItemsCana crearItemsCana(ItemsCana item){
        this.validator.validarDescripcion(item.getDescripcionItem());
        item.setEstadoItem(true);
        return this.repository.save(item);
    }

    @Override
    public void desactivarItem(Long idItem){
        ItemsCana item=this.repository.findById(idItem).get();
        item.setEstadoItem(false);
        this.repository.save(item);
    }

    @Override
    public void modificarItem(ItemsCana item, Long idItem){
        ItemsCana item2=this.repository.findById(idItem).get();
        this.validator.validarDescripcionEdicion(item2.getDescripcionItem(),idItem);
        item2.setDescripcionItem(item.getDescripcionItem());
        item2.setCostoItem(item.getCostoItem());
        item2.setCantidadItem(item.getCantidadItem());
        item2.setObservaciones(item.getObservaciones());
        item2.setCantidadFaltante(item.getCantidadFaltante());

        this.repository.save(item2);
    }
}
