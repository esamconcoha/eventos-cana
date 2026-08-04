package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mantenimiento_salones", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MantenimientoSalones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salon")
    private Long idSalon;

    @Column(name = "nombre_salon")
    private String nombreSalon;

    @Column(name = "direccion_salon")
    private String direccionSalon;

    @Column(name = "estado_salon")
    private Boolean estadoSalon;
}
