package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_catalogos_cana", schema = "cana")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoCatalogosCana {
    @Id
    @Column(name = "id_tipo_catalogo")
    private Long idTipoCatalogo;

    @Column(name = "nombre_catalogo")
    private String nombreCatalogo;

    @Column(name = "descripcion_catalogo")
    private String descripcionCatalogo;

    @Column(name = "fecha_creo")
    private LocalDateTime fechaCreo;

    @Column(name = "usuario_creo")
    private String usuarioCreo;

    @Column(name = "estado_registro")
    private Boolean estadoRegistro;


}
