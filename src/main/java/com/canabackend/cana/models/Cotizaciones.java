package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cotizaciones", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cotizaciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cotizacion")
    private Long idCotizacion;

    @Column(name = "nombre_cliente_cotizacion")
    private String nombreClienteCotizacion;

    @Column(name = "direccion_cliente_cotizacion")
    private String direccionClienteCotizacion;

    @Column(name = "telefono_cliente_cotizacion")
    private Long telefonoClienteCotizacion;

    @Column(name = "fecha_cotizacion")
    private LocalDate fechaCotizacion;

    @Column(name = "cotizacion_confirmada")
    private Boolean cotizacionConfirmada;

    @Column(name = "dpi_nit_usuario_cotizacion")
    private String dpiNitUsuarioCotizacion;

    @Column(name = "estado_cotizacion")
    private String estadoCotizacion;

    @Column (name= "fecha_hora_evento")
    private LocalDateTime fechaHoraEvento;

    @Column (name="cod_tipo_evento")
    private String codTipoEvento;

}
