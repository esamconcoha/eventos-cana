package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas_pedido",schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntregasPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long idEntrega;

    @Column(name = "correlativo_pedido")
    private String correlativoPedido;

    /** 'ENT' (ida) o 'REC' (vuelta). Ver MovimientoConstants. */
    @Column(name = "tipo_movimiento")
    private String tipoMovimiento;

    // integer en BD, no bigint como los ids
    @Column(name = "cantidad_viajes_aproximados")
    private Integer cantidadViajesAproximados;

    @Column(name = "cantidad_viajes_reales")
    private Integer cantidadViajesReales;

    @Column(name = "pedido_finalizado")
    private Boolean pedidoFinalizado;

    @Column(name = "fecha_inicio_entrega")
    private LocalDateTime fechaInicioEntrega;

    @Column(name = "fecha_fin_entrega")
    private LocalDateTime fechaFinEntrega;


}
