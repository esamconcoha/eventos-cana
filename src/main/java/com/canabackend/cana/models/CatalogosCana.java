package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalogos_cana", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogosCana {
    @Id
    @Column(name = "id_catalogo")
    private Long idCatalogo;

    @Column(name = "id_tipo_catalogo")
    private Long idTipoCatalogo;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "estado_registro")
    private Boolean estadoRegistro;
}
