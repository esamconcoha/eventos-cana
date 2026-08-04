package com.canabackend.cana.repositories;

import com.canabackend.cana.models.EstadosPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadosPedidoRepository extends JpaRepository<EstadosPedido,Long> {

    Optional<EstadosPedido> findFirstByCorrelativoPedidoAndFechaHoraFinIsNull(String correlativoPedido);

    List<EstadosPedido> findByCorrelativoPedidoOrderByFechaHoraInicioDesc(String correlativoPedido);
}
