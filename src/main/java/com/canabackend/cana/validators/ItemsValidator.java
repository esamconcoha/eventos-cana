package com.canabackend.cana.validators;

import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.repositories.ItemsCanaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("items")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ItemsValidator extends AbstractValidator{
    @Autowired
    private ItemsCanaRepository repository;

    public void validarDescripcion(String descripcion){
       Boolean existeDescripcion= this.repository.existeDescripcion(descripcion);
        if(existeDescripcion){
            throw new MSCanaException(ErrorEnum.DESCRIPCION_ITEM_EXISTENTE);
        }
    }

    public void validarDescripcionEdicion(String descripcion, Long idItem) {
        Boolean existeDescripcion = this.repository.existeDescripcionEdicion(descripcion, idItem);
        if (existeDescripcion) {
            throw new MSCanaException(ErrorEnum.DESCRIPCION_ITEM_EXISTENTE);
        }
    }
}
