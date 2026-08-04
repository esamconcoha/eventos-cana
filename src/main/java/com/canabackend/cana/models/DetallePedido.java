package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_pedido", schema = "cana")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @Column(name = "id_item")
    private Long idItem;

    @Column(name = "cantidad_item_pedido")
    private Double cantidadItemPedido;

    @Column(name = "correlativo_pedido")
    private String correlativoPedido;


}
