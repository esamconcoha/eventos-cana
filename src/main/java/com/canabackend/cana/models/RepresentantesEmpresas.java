package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "representantes_empresas", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepresentantesEmpresas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_representacion")
    private Long idRepresentacion;

    @Column(name = "dpi_usuario")
    private String dpiUsuario;

    @Column(name = "nit_empresa")
    private String nitEmpresa;

}
