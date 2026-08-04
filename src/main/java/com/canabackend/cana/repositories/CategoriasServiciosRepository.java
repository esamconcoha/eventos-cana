package com.canabackend.cana.repositories;

import com.canabackend.cana.models.CategoriasServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriasServiciosRepository extends JpaRepository<CategoriasServicio, Long> {

    @Query(value = "SELECT * FROM cana.categorias_servicio cs \n" +
            "where cs.estado_registro is true", nativeQuery = true)
    List<CategoriasServicio> listarCategoriasActivas();
}
