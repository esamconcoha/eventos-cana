package com.canabackend.cana.services;

import com.canabackend.cana.dtos.GetItemsExistentesDto;
import com.canabackend.cana.models.ItemsCana;

import java.util.List;

public interface ItemsCanaSvc {
    public List<GetItemsExistentesDto> getItemsExistentes();
    public ItemsCana crearItemsCana(ItemsCana item);
    public void desactivarItem(Long idItem);
    public void modificarItem(ItemsCana item, Long idItem);
}
