package com.canabackend.cana.repositories;

import com.canabackend.cana.models.DetalleViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleViajeRepository extends JpaRepository<DetalleViaje, Long> {

    List<DetalleViaje> findByIdEntregaOrderByFechaInicioViajeAscIdViajeAsc(Long idEntrega);

    long countByIdEntrega(Long idEntrega);
}
