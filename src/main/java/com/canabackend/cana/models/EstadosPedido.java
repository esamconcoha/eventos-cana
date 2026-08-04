package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "estados_pedido",schema = "cana")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class EstadosPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "correlativo_estado")
    private Long correlativoEstado;

    @Column(name = "correlativo_pedido")
    private String correlativoPedido;

    @Column(name = "id_estado")
    private Long idEstado;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;


}
