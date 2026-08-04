package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "servicios_decoracion", schema = "cana")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiciosDecoracion {
    @Id
    @Column(name = "id_servicio")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServicio;

    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(name = "nombre_servicio")
    private String nombreServicio;

    @Column(name = "descripcion_servicio")
    private String descripcionServicio;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "requiere_detalle")
    private Boolean requiereDetalle;

    @Column(name = "estado_servicio")
    private Boolean estadoServicio;

}
