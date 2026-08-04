package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "estados_pago_pedido", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadosPagoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "correlativo_estado_pago")
    private Long correlativoEstadoPago;

    @Column(name = "correlativo_pedido")
    private String correlativoPedido;

    @Column(name = "estado_pago")
    private String estadoPago;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;
}
