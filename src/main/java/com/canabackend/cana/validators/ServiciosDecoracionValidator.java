package com.canabackend.cana.validators;

import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.repositories.ServiciosDecoracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("ServiciosDecoracion")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ServiciosDecoracionValidator extends  AbstractValidator{
    @Autowired
    private ServiciosDecoracionRepository repository;

    public void validarNombre(String nombre){
        Boolean existeNombre= this.repository.existsByNombre(nombre);
        if(existeNombre){
            throw new MSCanaException(ErrorEnum.NOMBRE_SERVICIO_EXISTENTE);
        }
    }

    public void validarNombreEdicion(String nombre, Long idServicio){
        Boolean existeNombre = this.repository.existsByNombreEdicion(nombre, idServicio);
        if(existeNombre){
            throw new MSCanaException(ErrorEnum.NOMBRE_SERVICIO_EXISTENTE);
        }
    }
}
