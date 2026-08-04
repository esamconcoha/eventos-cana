package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categorias_servicio", schema = "cana")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoriasServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(name = "nombre_categoria")
    private String nombreCategoria;

    @Column(name = "tipo_categoria")
    private String tipoCategoria;

    @Column(name = "estado_registro")
    private Boolean estadoRegistro;

}
