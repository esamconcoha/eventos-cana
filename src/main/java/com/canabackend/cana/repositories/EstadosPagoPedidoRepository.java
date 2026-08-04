package com.canabackend.cana.repositories;

import com.canabackend.cana.models.EstadosPagoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadosPagoPedidoRepository extends JpaRepository<EstadosPagoPedido, Long> {

    Optional<EstadosPagoPedido> findFirstByCorrelativoPedidoAndFechaHoraFinIsNull(String correlativoPedido);

    List<EstadosPagoPedido> findByCorrelativoPedidoOrderByFechaHoraInicioDesc(String correlativoPedido);
}
