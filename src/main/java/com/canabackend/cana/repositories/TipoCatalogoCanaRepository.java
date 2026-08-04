package com.canabackend.cana.repositories;

import com.canabackend.cana.models.TipoCatalogosCana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoCatalogoCanaRepository extends JpaRepository<TipoCatalogosCana, Long> {
}
