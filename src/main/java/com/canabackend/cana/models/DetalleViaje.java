package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_viaje",schema = "cana")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetalleViaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_viaje")
    private Long idViaje;

    @Column(name = "id_entrega")
    private Long idEntrega;

    @Column(name = "fecha_inicio_viaje")
    private LocalDateTime fechaInicioViaje;

    @Column(name = "fecha_fin_viaje")
    private LocalDateTime fechaFinViaje;


}
