package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_servicio_cotizacion", schema = "cana")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DetalleServicioCotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_serv_cotiz")
    private Long idDetalleServCotiz;

    @Column(name = "id_cotizacion")
    private Long idCotizacion;

    @Column(name = "id_servicio")
    private Long idServicio;

    @Column(name = "cantidad")
    private double cantidad;

    @Column(name = "precio_cotizado")
    private double precioCotizado;

    @Column(name = "especificaciones", columnDefinition = "TEXT")
    private String especificaciones;

}
