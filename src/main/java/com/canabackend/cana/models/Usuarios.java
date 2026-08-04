package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "usuarios", schema = "cana")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuarios  {
    @Id
    @Column(name = "dpi_nit_usuario")
    private String dpiNitUsuario;

    @Column(name = "nombres_usuario")
    private String nombresUsuario;

    @Column(name = "apellidos_usuario")
    private String apellidosUsuario;

    @Column(name = "telefono_usuario")
    private Long telefonoUsuario;

    @Column(name = "correo")
    private String correo;

    @Column(name = "es_representante")
    private Boolean esRepresentante;

    @Column(name = "estado_usuario")
    private Boolean estadoUsuario;

    @Column(name = "rol")
    private Long rol;

    @Column(name = "es_empresa")
    private Boolean esEmpresa;

    @Column(name = "contrasenia")
    private String contrasenia;

    @Column(name = "usuario_creo")
    private String usuarioCreo;

}
