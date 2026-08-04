package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items_cana", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemsCana {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @Column(name = "id_tipo_item")
    private Long idTipoItem;

    @Column(name = "costo_item")
    private Double costoItem;

    @Column(name = "descripcion_item")
    private String descripcionItem;

    @Column(name = "cantidad_item")
    private Long cantidadItem;

    @Column(name="observaciones", length = 200)
    private String observaciones;

    @Column(name="cantidad_faltantes")
    private Integer cantidadFaltante;

    @Column(name="estado_item")
    private Boolean estadoItem;
}
