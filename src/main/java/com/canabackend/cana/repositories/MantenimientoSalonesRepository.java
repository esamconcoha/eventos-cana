package com.canabackend.cana.repositories;

import com.canabackend.cana.models.MantenimientoSalones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MantenimientoSalonesRepository extends JpaRepository<MantenimientoSalones, Long> {
}
