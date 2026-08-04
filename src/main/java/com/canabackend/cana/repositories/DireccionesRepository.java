package com.canabackend.cana.repositories;

import com.canabackend.cana.models.Direcciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionesRepository extends JpaRepository<Direcciones, Long> {
    List<Direcciones> findDireccionesBynitDpi(String dpiNitUsuario);
}
