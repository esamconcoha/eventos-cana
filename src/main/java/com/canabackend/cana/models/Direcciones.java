package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "direcciones", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Direcciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long idDireccion;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "nit_dpi")
    private String nitDpi;
}
