package com.canabackend.cana.repositories;

import com.canabackend.cana.models.Estados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadosRepository extends JpaRepository<Estados, Long> {

    Optional<Estados> findByTipoEstadoAndCodigoEstado(String tipoEstado, String codigoEstado);
}
