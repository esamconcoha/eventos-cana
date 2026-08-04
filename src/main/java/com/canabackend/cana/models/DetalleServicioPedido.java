package com.canabackend.cana.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_servicio_pedido", schema = "cana")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DetalleServicioPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_serv_pedido")
    private Long idDetalleServPedido;

    @Column(name = "correlativo_pedido")
    private String correlativoPedido;

    @Column(name = "id_servicio")
    private Long idServicio;

    @Column(name = "cantidad")
    private double cantidad;

    @Column(name = "precio_acordado")
    private double precioAcordado;

    @Column(name = "especificaciones", columnDefinition = "TEXT")
    private String especificaciones;

    /**
     * Cuando se confirmo realizado el servicio. NULL = pendiente.
     *
     * El montaje vive aca y no en el ciclo de vida del pedido porque es
     * paralelo a los viajes de entrega (pueden ocurrir a la vez) y opcional
     * (solo los pedidos que compraron el servicio), y una maquina de estados
     * no admite ninguna de las dos cosas.
     */
    @Column(name = "fecha_realizado")
    private LocalDateTime fechaRealizado;



}
