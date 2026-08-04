package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DocumentosCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentosCotizacionRepository extends JpaRepository<DocumentosCotizacion, Long> {

    /**
     * Ultimo documento vigente generado para una cotizacion.
     */
    Optional<DocumentosCotizacion> findFirstByIdCotizacionAndEstadoRegistroTrueOrderByFechaGeneracionDesc(Long idCotizacion);
}
