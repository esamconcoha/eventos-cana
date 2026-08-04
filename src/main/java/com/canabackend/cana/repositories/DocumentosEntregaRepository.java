package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DocumentosEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentosEntregaRepository extends JpaRepository<DocumentosEntrega, Long> {

    /** Ultimo documento vigente subido para una entrega. */
    Optional<DocumentosEntrega> findFirstByIdEntregaAndEstadoRegistroTrueOrderByFechaGeneracionDesc(Long idEntrega);

    /** Para el flag "tiene constancia firmada" del detalle, sin traer el contenido. */
    boolean existsByIdEntregaAndEstadoRegistroTrue(Long idEntrega);
}
