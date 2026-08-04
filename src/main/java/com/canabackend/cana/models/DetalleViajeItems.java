package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_viaje_items", schema = "cana")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetalleViajeItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @Column(name = "id_item")
    private Long idItem;

    @Column(name = "id_detalle_viaje")
    private Long idDetalleViaje;

    @Column(name = "cantidad_item")
    private Double cantidadItem;

}
