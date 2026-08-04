package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estados", schema = "cana")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Estados {
    @Id
    @Column(name = "id_estado")
    private Long idEstado;

    @Column(name = "tipo_estado")
    private String tipoEstado;

    @Column(name = "codigo_estado")
    private String codigoEstado;

    @Column(name = "nombre_estado")
    private String nombreEstado;

    @Column(name = "descripcion_estado")
    private String descripcionEstado;

    @Column(name = "estado_registro")
    private Boolean estadoRegistro;

}
