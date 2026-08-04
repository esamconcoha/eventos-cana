package com.canabackend.cana.repositories;

import com.canabackend.cana.models.RepresentantesEmpresas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepresentantesEmpresasRepository extends JpaRepository<RepresentantesEmpresas,Long> {
}
